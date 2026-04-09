package com.spy.server.controller;


import com.spy.server.common.BaseResponse;
import com.spy.server.common.DeleteRequest;
import com.spy.server.common.ErrorCode;
import com.spy.server.constant.UserConstant;
import com.spy.server.exception.BusinessException;
import com.spy.server.file.CosManager;
import com.spy.server.file.FileConstant;
import com.spy.server.model.domain.Shop;
import com.spy.server.model.domain.ShopMedia;
import com.spy.server.model.domain.User;
import com.spy.server.model.vo.ShopMediaVO;
import com.spy.server.service.ShopMediaService;
import com.spy.server.service.ShopService;
import com.spy.server.service.UserService;
import com.spy.server.utils.ResultUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    private static final int SHOP_MEDIA_TYPE_IMAGE = 1;
    private static final int SHOP_MEDIA_TYPE_VIDEO = 2;
    private static final int MAX_SHOP_IMAGE_COUNT = 10;
    private static final int MAX_SHOP_VIDEO_COUNT = 3;
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private ShopService shopService;

    @Resource
    private ShopMediaService shopMediaService;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) throws BusinessException {
        User loginUser = userService.getLoginUser(request);
        validateImageFile(multipartFile, MAX_IMAGE_SIZE);
        String filepath = buildFilePath("common", String.valueOf(loginUser.getId()), multipartFile.getOriginalFilename());
        String url = uploadMultipartFile(multipartFile, filepath);
        return ResultUtil.success(url);
    }

    @PostMapping("/avatar/upload")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile,
                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        validateImageFile(multipartFile, MAX_AVATAR_SIZE);
        String filepath = buildFilePath("avatar", String.valueOf(loginUser.getId()), multipartFile.getOriginalFilename());
        String url = uploadMultipartFile(multipartFile, filepath);
        User user = new User();
        user.setId(loginUser.getId());
        user.setAvatar(url);
        boolean updated = userService.updateById(user);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像更新失败");
        }
        return ResultUtil.success(url);
    }

    @PostMapping("/shop/image/upload")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<List<ShopMediaVO>> uploadShopImages(@RequestParam("shopId") Long shopId,
                                                            @RequestPart("files") MultipartFile[] multipartFiles,
                                                            HttpServletRequest request) {
        return ResultUtil.success(uploadShopMedia(shopId, multipartFiles, SHOP_MEDIA_TYPE_IMAGE, request));
    }

    @PostMapping("/shop/video/upload")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<List<ShopMediaVO>> uploadShopVideos(@RequestParam("shopId") Long shopId,
                                                            @RequestPart("files") MultipartFile[] multipartFiles,
                                                            HttpServletRequest request) {
        return ResultUtil.success(uploadShopMedia(shopId, multipartFiles, SHOP_MEDIA_TYPE_VIDEO, request));
    }

    @GetMapping("/shop/media/list")
    public BaseResponse<List<ShopMediaVO>> listShopMedia(@RequestParam("shopId") Long shopId) {
        if (shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺参数错误");
        }
        List<ShopMedia> shopMediaList = shopMediaService.listByShopId(shopId);
        return ResultUtil.success(shopMediaService.getShopMediaVOList(shopMediaList));
    }

    @PostMapping("/shop/media/delete")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> deleteShopMedia(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ShopMedia shopMedia = shopMediaService.getById(deleteRequest.getId());
        if (shopMedia == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "素材不存在");
        }
        Shop shop = shopService.getById(shopMedia.getShopId());
        validateShopMediaPermission(loginUser, shop);

        boolean removed = shopMediaService.removeById(shopMedia.getId());
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "素材删除失败");
        }
        deleteCosObjectByUrl(shopMedia.getUrl());
        if (shopMedia.getType() != null
                && shopMedia.getType() == SHOP_MEDIA_TYPE_IMAGE
                && shopMedia.getIsCover() != null
                && shopMedia.getIsCover() == 1) {
            List<ShopMedia> remainImageList = shopMediaService.listByShopIdAndType(shopMedia.getShopId(), SHOP_MEDIA_TYPE_IMAGE);
            if (!remainImageList.isEmpty()) {
                ShopMedia nextCover = remainImageList.get(0);
                ShopMedia updateMedia = new ShopMedia();
                updateMedia.setId(nextCover.getId());
                updateMedia.setIsCover(1);
                shopMediaService.updateById(updateMedia);
            }
        }
        return ResultUtil.success(true);
    }

    private List<ShopMediaVO> uploadShopMedia(Long shopId,
                                              MultipartFile[] multipartFiles,
                                              Integer mediaType,
                                              HttpServletRequest request) {
        if (shopId == null || shopId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "店铺参数错误");
        }
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先选择文件");
        }
        User loginUser = userService.getLoginUser(request);
        Shop shop = shopService.getById(shopId);
        if (shop == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "店铺不存在");
        }
        validateShopMediaPermission(loginUser, shop);

        int maxCount = mediaType != null && mediaType == SHOP_MEDIA_TYPE_IMAGE ? MAX_SHOP_IMAGE_COUNT : MAX_SHOP_VIDEO_COUNT;
        long currentCount = shopMediaService.countByShopIdAndType(shopId, mediaType);
        if (currentCount + multipartFiles.length > maxCount) {
            String mediaText = mediaType != null && mediaType == SHOP_MEDIA_TYPE_IMAGE ? "图片" : "视频";
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该店铺最多上传 " + maxCount + " 个" + mediaText);
        }

        List<ShopMedia> existingMediaList = shopMediaService.listByShopIdAndType(shopId, mediaType);
        int nextSortNo = existingMediaList.stream()
                .map(ShopMedia::getSortNo)
                .filter(sortNo -> sortNo != null)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;

        List<ShopMedia> savedMediaList = new ArrayList<>(multipartFiles.length);
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "包含空文件，无法上传");
            }
            if (mediaType != null && mediaType == SHOP_MEDIA_TYPE_IMAGE) {
                validateImageFile(multipartFile, MAX_IMAGE_SIZE);
            } else {
                validateVideoFile(multipartFile, MAX_VIDEO_SIZE);
            }
            String mediaFolder = mediaType != null && mediaType == SHOP_MEDIA_TYPE_IMAGE ? "image" : "video";
            String filepath = buildFilePath("shop/" + shopId + "/" + mediaFolder, String.valueOf(loginUser.getId()),
                    multipartFile.getOriginalFilename());
            String url = uploadMultipartFile(multipartFile, filepath);

            ShopMedia shopMedia = new ShopMedia();
            shopMedia.setShopId(shopId);
            shopMedia.setType(mediaType);
            shopMedia.setUrl(url);
            shopMedia.setSortNo(nextSortNo++);
            shopMedia.setIsCover(savedMediaList.isEmpty() && existingMediaList.isEmpty() && mediaType != null && mediaType == SHOP_MEDIA_TYPE_IMAGE ? 1 : 0);
            shopMedia.setFileName(multipartFile.getOriginalFilename());
            shopMedia.setContentType(multipartFile.getContentType());
            shopMedia.setFileSize(multipartFile.getSize());
            shopMedia.setCreateUserId(loginUser.getId());
            boolean saved = shopMediaService.save(shopMedia);
            if (!saved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "素材保存失败");
            }
            savedMediaList.add(shopMedia);
        }

        return shopMediaService.getShopMediaVOList(savedMediaList);
    }

    private void validateShopMediaPermission(User loginUser, Shop shop) {
        if (loginUser == null || shop == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        String userRole = loginUser.getUserRole();
        boolean isAdmin = StringUtils.equals(userRole, UserConstant.ADMIN_ROLE);
        boolean isManager = StringUtils.equals(userRole, UserConstant.MANAGER_ROLE)
                && loginUser.getId() != null
                && loginUser.getId().equals(shop.getManagerId());
        if (!isAdmin && !isManager) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权管理该店铺素材");
        }
    }

    private void validateImageFile(MultipartFile multipartFile, long maxSize) {
        validateFileSize(multipartFile, maxSize);
        String contentType = multipartFile.getContentType();
        if (StringUtils.isBlank(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持上传图片文件");
        }
    }

    private void validateVideoFile(MultipartFile multipartFile, long maxSize) {
        validateFileSize(multipartFile, maxSize);
        String contentType = multipartFile.getContentType();
        if (StringUtils.isBlank(contentType) || !contentType.startsWith("video/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持上传视频文件");
        }
    }

    private void validateFileSize(MultipartFile multipartFile, long maxSize) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        if (multipartFile.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件过大，请压缩后重试");
        }
    }

    private String buildFilePath(String biz, String operatorId, String originalFilename) {
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String safeFileName = StringUtils.defaultIfBlank(originalFilename, "file")
                .replace("\\", "_")
                .replace("/", "_")
                .replace(" ", "_");
        return String.format("/%s/%s/%s-%s", biz, operatorId, uuid, safeFileName);
    }

    private String uploadMultipartFile(MultipartFile multipartFile, String filepath) {
        File file = null;
        try {
            file = File.createTempFile("cos-upload-", null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            return FileConstant.COS_HOST + filepath;
        } catch (Exception e) {
            log.error("文件上传失败：文件路径={}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("临时文件删除失败：文件路径={}", filepath);
                }
            }
        }
    }

    private void deleteCosObjectByUrl(String url) {
        if (StringUtils.isBlank(url) || !url.startsWith(FileConstant.COS_HOST)) {
            return;
        }
        String key = url.substring(FileConstant.COS_HOST.length());
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            cosManager.deleteObject(key);
        } catch (Exception e) {
            log.warn("COS 对象删除失败：对象键={}", key, e);
        }
    }
}

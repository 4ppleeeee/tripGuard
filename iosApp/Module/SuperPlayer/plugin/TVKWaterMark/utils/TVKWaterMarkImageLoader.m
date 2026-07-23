/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkImageLoader.m
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/12/19
 Description :
 History     : 2017/12/19 初始版本
 ***********************************************************/

#import "TVKWaterMarkImageLoader.h"
#import "SPNetWorkManager.h"
#import "YYImageCache.h"
#import "TVKThreadUtils.h"

@implementation TVKWaterMarkImageLoader

- (void)loadWithUrl:(NSString *)urlStr MD5:(NSString *)MD5 completion:(TVKWaterMarkImageLoaderCompletionBlock)completion {
    UIImage *image = [[YYImageCache sharedCache] getImageForKey:MD5];
    if (!image) {
        // 从下载尝试读取一下，下载组件下载视频时会把水印logo下载下来
        // TODO: haitendxia
        /*
        NSString *path = [[TVKP2PManager sharedInstance] getOfflineLogoWithMD5:MD5];
        if (path.length > 0) {
            image = [UIImage imageWithContentsOfFile:path];
        }
         */
    }

    if (image) {
        tvk_dispatch_main_async_safe(^{
            if (completion) {
                completion(image, nil);
            }
        });

        return;
    }

    [[SPNetWorkManager shareInstance] getRequest:urlStr
                                   requestHeaders:nil
                                completionHandler:^(NSData *__nullable responseData, NSError *__nullable error) {
                                    UIImage *image = [UIImage imageWithData:responseData];
                                    tvk_dispatch_main_async_safe(^{
                                        if (completion) {
                                            completion(image, error);
                                            if (image) {
                                                [[YYImageCache sharedCache] setImage:image forKey:MD5];
                                            }
                                        }
                                    });
                                }];
}

@end

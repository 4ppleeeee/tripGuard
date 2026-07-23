/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetWorkManager.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 16/3/28
 Description :
 History     : 16/3/28 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPNetWorkResultCode.h"

typedef void (^GetPostRequestCompletionHandlerBlock)(NSData *__nullable responseData, NSError *__nullable error);

/**
 *  这个类来负责处理jce请求，这个类是个单例模式。使用的时候请注意了
 */
@interface SPNetWorkManager : NSObject

+ (nonnull instancetype)shareInstance;

/**
 发送一个get请求

 @param url
 @param requestHeaders 需要自定义的http头
 @param completionHandler 完成时候的block
 @return 返回一个句柄，方便用来cancel
 */
- (nullable id)getRequest:(nonnull NSString *)url
           requestHeaders:(nullable NSDictionary *)requestHeaders
        completionHandler:(nullable GetPostRequestCompletionHandlerBlock)completionHandler;

/**
 发送一个Post请求

 @param url
 @param requestHeaders 需要自定义的http头
 @param postData 需要post的body的数据，也可以为nil
 @param completionHandler 完成的block回调
 @return 返回一个句柄，用来cancel
 */
- (nullable id)postRequest:(nonnull NSString *)url
            requestHeaders:(nullable NSDictionary *)requestHeaders
                  postData:(nullable NSData *)postData
         completionHandler:(nullable GetPostRequestCompletionHandlerBlock)completionHandler;

/**
 *  取消特定的请求,这里需要注意，如果请求被取消，就不再有任何状态回调了
 *
 *  @param task sendJCERequest 返回的上下文，不能为nil
 */
- (void)cancelRequestWithTask:(nonnull id)task;

@end

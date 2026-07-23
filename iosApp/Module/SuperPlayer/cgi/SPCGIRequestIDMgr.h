//
//  SPCGIRequestIDMgr.h
//  SPPlayer
//
//  Created by hemanli on 2019/10/20.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SPCGIRequestIDMgr : NSObject

+ (instancetype)sharedInstance;

- (int)generateGetVInfoRequestID;

- (int)generateGetVBKeyRequestID;

- (int)generateGetLiveRequestID;

@end

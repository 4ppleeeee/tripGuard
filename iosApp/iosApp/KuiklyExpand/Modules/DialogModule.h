//
//  DialogModule.h
//  iosApp
//
//  Created by smart on 2025/4/6.
//  Copyright © 2025 Tencent. All rights reserved.
//

#import "KRBaseModule.h"

NS_ASSUME_NONNULL_BEGIN

#define KMM_DIALOG_MODULE_ON_CLOSE @"DialogModuleOnCloseDialog"
#define KMM_DIALOG_MODULE_NAV_TO @"DialogModuleNavigateTo"
#define KMM_DIALOG_MODULE_OPEN_COMMENT @"DialogModuleOpenComment"
#define KMM_DIALOG_MODULE_AGREE_AI_POD_CAST @"DialogModuleAgreeAIPodCast"
#define KMM_DIALOG_MODULE_RECEIVE_AI_AVATAR @"DialogModuleReceiveAIAvatar"
#define KMM_DIALOG_MODULE_ACTIVITY_DEALLOC @"kQNComposeActivityControllerDealloc"
#define KMM_DIALOG_MODULE_MESSAGE_LIST_SIZE_CHANGED @"DialogModuleMessageListSizeChanged"


@interface DialogModule : KRBaseModule

@end

NS_ASSUME_NONNULL_END

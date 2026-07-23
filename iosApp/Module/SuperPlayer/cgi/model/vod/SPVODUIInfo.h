/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPVODUIInfo.h
 * @brief    点播getvinfo返回的ui节点信息
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/21
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>

@interface SPVODUIInfo : NSObject

@property (nonatomic, copy) NSString *urlStr;

@property (nonatomic, copy) NSString *vt;  // CDN 编号

@property (nonatomic, copy) NSString *pt;  // ui.hls节点下的pt字段

// 免流相关参数，不知道啥意思，请求getvbkey时用到
@property (nonatomic, strong) NSString *spip;
@property (nonatomic, strong) NSString *spport;
@property (nonatomic, strong) NSString *path;

@end

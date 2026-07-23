/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPPlayCommonParam.h
 * @brief    播放上下文，用来指定在请求cgi时满足当前播放的一些参数
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/21
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>

// SPMediaPlayer下各个子对象初始化时需要传递的参数，注意这里只存放一次播放实例生命周期内不会改变的参数

@interface SPPlayCommonParam : NSObject

@property (nonatomic, assign) int playerSeq;  // 播放器实例sequence

@property (nonatomic, strong) dispatch_queue_t playerQueue;  // 播放工作线程

@end

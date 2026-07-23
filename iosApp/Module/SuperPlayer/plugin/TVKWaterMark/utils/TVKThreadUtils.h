/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     TVKThreadUtils.h
 * @brief    线程工具类
 * @author   andygao
 * @version  1.0.0
 * @date     2019/6/3
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

// tvk_dispatch_main_async_safe 判断当前是否是主线程否，如果是则直接执行，不再转换线程
static inline void tvk_dispatch_main_async_safe(dispatch_block_t block) {
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_async(dispatch_get_main_queue(), block);
    }
}

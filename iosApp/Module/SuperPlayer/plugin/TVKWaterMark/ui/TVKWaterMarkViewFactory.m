/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkFactory.m
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/
//

#import "TVKWaterMarkViewFactory.h"

@interface TVKWaterMarkViewFactory ()
@property (nonatomic, strong) NSMutableDictionary *waterMarks;
@end

@implementation TVKWaterMarkViewFactory

- (TVKWaterMarkView *)createWaterMarkView:(TVKWaterMarkInfo *)waterMarkInfo {
    TVKWaterMarkView *waterMarkView = [self getWaterMarkView:waterMarkInfo];
    if (!waterMarkView) {
        waterMarkView = [[TVKWaterMarkView alloc] initWithWaterMarkInfo:waterMarkInfo];
        [self addWaterMark:waterMarkView waterMarkInfo:waterMarkInfo];
    }

    return waterMarkView;
}

- (TVKWaterMarkView *)queryWaterMarkView:(TVKWaterMarkInfo *)waterMarkInfo {
    return [self getWaterMarkView:waterMarkInfo];
}

- (NSMutableDictionary *)waterMarks {
    if (!_waterMarks) {
        _waterMarks = [[NSMutableDictionary alloc] init];
    }
    return _waterMarks;
}

- (TVKWaterMarkView *)getWaterMarkView:(TVKWaterMarkInfo *)waterMarkInfo {
    if (!waterMarkInfo) {
        return nil;
    }
    TVKWaterMarkView *waterMark = nil;
    NSString *key = [self keyWithWaterMarkInfo:waterMarkInfo];
    if (key.length) {
        waterMark = [self.waterMarks objectForKey:key];
    }
    return waterMark;
}

- (void)removeAll {
    [self.waterMarks enumerateKeysAndObjectsUsingBlock:^(NSString *_Nonnull key, TVKWaterMarkView *_Nonnull obj, BOOL *_Nonnull stop) {
        [self destroyWaterMarkView:obj];
    }];
    [self.waterMarks removeAllObjects];
}

- (void)addWaterMark:(TVKWaterMarkView *)waterMark waterMarkInfo:(TVKWaterMarkInfo *)waterMarkInfo {
    if (!waterMarkInfo || !waterMark) {
        return;
    }
    NSString *key = [self keyWithWaterMarkInfo:waterMarkInfo];
    if (key.length) {
        [self.waterMarks setObject:waterMark forKey:key];
    }
}

- (TVKWaterMarkView *)removeWaterMarkViewWithInfo:(TVKWaterMarkInfo *)waterMarkInfo {
    NSString *key = [self keyWithWaterMarkInfo:waterMarkInfo];

    TVKWaterMarkView *waterMark = [self.waterMarks objectForKey:key];
    [self destroyWaterMarkView:waterMark];
    if (key.length) {
        [self.waterMarks removeObjectForKey:key];
    }
    return waterMark;
}

- (NSString *)keyWithWaterMarkInfo:(TVKWaterMarkInfo *)waterMarkInfo {
    NSUInteger hash = [waterMarkInfo hash];
    return [NSString stringWithFormat:@"%lu", (unsigned long)hash];
}

- (void)destroyWaterMarkView:(TVKWaterMarkView *)waterMarkView {
    [waterMarkView destroy];
}
@end

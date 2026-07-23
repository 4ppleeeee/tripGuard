//
//  SPSection.m
//  SPPlayer
//
//  Created by liyukuan on 2019/10/3.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPSection.h"

@implementation SPSection

- (NSString *)description {
    return [NSString stringWithFormat:@"url:%@, duration:%f, urlList:%@, size=%lld", self.url, self.duration, self.urlList, self.clipSize];
}
@end

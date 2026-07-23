//
//  reportProtocol.h
//  oclibTest
//
//  Created by gabyxie(谢琳) on 2019/9/24.
//  Copyright © 2019 gabyxie(谢琳). All rights reserved.
//

#ifndef reportProtocol_h
#define reportProtocol_h

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol reportProtocol <NSObject>

- (void)report: (NSDictionary*)reportInfo;

@end

NS_ASSUME_NONNULL_END

#endif /* reportProtocol_h */

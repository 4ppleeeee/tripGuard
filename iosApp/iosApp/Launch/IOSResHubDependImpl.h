//
//  IOSResHubDependImpl.h
//  iosApp
//
//  ResHub SDK depends protocol implementation for wesee-core.
//  Uses default implementations from ResHub and RDelivery subspecs.
//

#ifndef IOSResHubDependImpl_h
#define IOSResHubDependImpl_h

#import <Foundation/Foundation.h>
#import <ResHub/ResHubDependProtocol.h>

NS_ASSUME_NONNULL_BEGIN

/// ResHubDependProtocol implementation for wesee-core iosApp.
/// Assembles default implementations from ResHub/DownloadImpl, ResHub/FileImpl,
/// ResHub/Beacon, RDelivery/DefaultStorageImpl, RDelivery/DefaultNetworkImpl,
/// RDelivery/DefaultJsonModelImpl, and RDelivery/DefaultLogImpl.
@interface IOSResHubDependImpl : NSObject <ResHubDependProtocol>

@end

NS_ASSUME_NONNULL_END

#endif /* IOSResHubDependImpl_h */

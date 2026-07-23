//
//  IOSResHubDependImpl.m
//  iosApp
//
//  ResHub SDK depends protocol implementation for wesee-core.
//

#import "IOSResHubDependImpl.h"
#import <RDelivery/RDNetworkImpl.h>
#import <RDelivery/RDMMKVFactoryImpl.h>
#import <RDelivery/RDLoggerImpl.h>
#import <RDelivery/RDeliveryJsonModelImpl.h>
#import <ResHub/ResHubFileImpl.h>
#import <ResHub/ResHubDownloadImpl.h>
#import <ResHub/ResHubBeaconImpl.h>

@implementation IOSResHubDependImpl

@synthesize kvFactoryImpl = _kvFactoryImpl;
@synthesize fileImpl = _fileImpl;
@synthesize netImpl = _netImpl;
@synthesize downloadImpl = _downloadImpl;
@synthesize beaconImpl = _beaconImpl;
@synthesize yymodelImpl = _yymodelImpl;
@synthesize logImpl = _logImpl;
@synthesize threadImpl;
@synthesize verControlImpl;
@synthesize downloadStorageImpl;
@synthesize autoUnzipImpl;
@synthesize presetImpl;
@synthesize remoteLoadInterceptImpl;

- (instancetype)init {
    self = [super init];
    if (self) {
        _kvFactoryImpl = [RDMMKVFactoryImpl sharedInstance];
        _fileImpl = [ResHubFileImpl sharedInstance];
        _netImpl = [RDNetworkImpl sharedInstance];
        _downloadImpl = [ResHubDownloadImpl sharedInstance];
        _beaconImpl = [ResHubBeaconImpl sharedInstance];
        _yymodelImpl = [RDeliveryJsonModelImpl sharedInstance];
        _logImpl = [RDLoggerImpl sharedInstance];
    }
    return self;
}

@end

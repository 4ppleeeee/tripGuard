#import "KRWSVideoView.h"
#import <KuiklyIOSRender/KuiklyRenderModuleExportProtocol.h>
#import <umbrella/umbrella.h>

// Event keys（与 DSL 层 WSVideoEvent 保持一致）
static NSString *const kEventOnSurfaceCreated = @"onSurfaceCreated";
static NSString *const kEventOnSurfaceDestroyed = @"onSurfaceDestroyed";

@interface KRWSVideoView ()

@property (nonatomic, copy) KuiklyRenderCallback onSurfaceCreatedCallback;
@property (nonatomic, copy) KuiklyRenderCallback onSurfaceDestroyedCallback;
@property (nonatomic, assign) BOOL surfaceReady;
@property (nonatomic, strong) NSString *surfaceId;

@end

@implementation KRWSVideoView

- (instancetype)init {
    if (self = [super init]) {
        _surfaceReady = NO;
    }
    return self;
}

#pragma mark - KuiklyRenderViewExportProtocol

- (void)hrv_callWithMethod:(NSString *)method
                    params:(NSString *)params
                  callback:(KuiklyRenderCallback)callback {
    KUIKLY_CALL_CSS_METHOD;
}

- (void)hrv_setPropWithKey:(NSString *)propKey propValue:(id)propValue {

    // 处理框架尺寸属性
    if ([propKey isEqualToString:@"frame"]) {
        // 检查并转换NSRect值
        if ([propValue isKindOfClass:[NSValue class]]) {
            // 设置视图框架
            CGRect frame = [(NSValue *)propValue CGRectValue];
            self.frame = frame;
            
        }
    }
    
    
    // 设置surface callback
    if ([propKey isEqualToString:kEventOnSurfaceCreated]) {
        self.onSurfaceCreatedCallback = propValue;
        // iOS 端 view 本身就是 surface，直接回调
        if (!self.surfaceReady) {
            self.surfaceReady = YES;
            if (self.onSurfaceCreatedCallback) {
                NSString *surfaceId =  [UmbrellaWSVideoViewMapping.shared rememberSurface:self];
                NSDictionary *surfaceDict = @{@"surfaceId": surfaceId};
                NSError *error;
                NSData *jsonData = [NSJSONSerialization dataWithJSONObject:surfaceDict
                                                                   options:NSJSONWritingPrettyPrinted
                                                                     error:&error];
                if (!jsonData) {
                    NSLog(@"Got an error: %@", error);
                } else {
                    self.surfaceId = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                }
                if (self.surfaceId) {
                    self.onSurfaceCreatedCallback(self.surfaceId);
                }
            }
        } else {
            if (self.surfaceId) {
                self.onSurfaceCreatedCallback(self.surfaceId);
            }
        }
    } else if ([propKey isEqualToString:kEventOnSurfaceDestroyed]) {
        self.onSurfaceDestroyedCallback = propValue;
    }

    KUIKLY_SET_CSS_COMMON_PROP;
}

#pragma mark - Lifecycle

- (void)removeFromSuperview {
    if (self.surfaceReady) {
        self.surfaceReady = NO;
        if (self.onSurfaceDestroyedCallback && self.surfaceId) {
            self.onSurfaceDestroyedCallback(self.surfaceId);
        }
    }
    [super removeFromSuperview];
}

- (void)dealloc {
    self.onSurfaceCreatedCallback = nil;
    self.onSurfaceDestroyedCallback = nil;
}

@end

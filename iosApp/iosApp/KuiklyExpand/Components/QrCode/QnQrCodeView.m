#import "QnQrCodeView.h"
#import <CoreImage/CoreImage.h>

@interface QnQrCodeView ()

@property (nonatomic, strong) UIImageView *imageView;
@property (nonatomic, strong) CIContext *ciContext;
@property (nonatomic, copy) NSString *content;
@property (nonatomic, strong) UIColor *foregroundColor;
@property (nonatomic, strong) UIColor *qrBackgroundColor;

@end

@implementation QnQrCodeView
@synthesize hr_rootView;

- (instancetype)init {
    if (self = [super init]) {
        _content = @"";
        _foregroundColor = UIColor.blackColor;
        _qrBackgroundColor = UIColor.whiteColor;
        _ciContext = [CIContext contextWithOptions:nil];
        _imageView = [[UIImageView alloc] initWithFrame:CGRectZero];
        _imageView.contentMode = UIViewContentModeScaleAspectFit;
        _imageView.backgroundColor = _qrBackgroundColor;
        [self addSubview:_imageView];
        self.backgroundColor = _qrBackgroundColor;
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.imageView.frame = self.bounds;
    [self refreshQrCodeImage];
}

- (void)hrv_setPropWithKey:(NSString * _Nonnull)propKey
                 propValue:(id _Nonnull)propValue {
    if ([propKey isEqualToString:@"frame"]) {
        if ([propValue isKindOfClass:[NSValue class]]) {
            self.frame = [(NSValue *)propValue CGRectValue];
        }
    } else if ([propKey isEqualToString:@"content"]) {
        self.content = [propValue isKindOfClass:[NSString class]] ? propValue : [propValue description];
        [self refreshQrCodeImage];
    } else if ([propKey isEqualToString:@"foregroundColor"]) {
        self.foregroundColor = [self colorWithValue:propValue defaultColor:UIColor.blackColor];
        [self refreshQrCodeImage];
    } else if ([propKey isEqualToString:@"backgroundColor"]) {
        self.qrBackgroundColor = [self colorWithValue:propValue defaultColor:UIColor.whiteColor];
        self.backgroundColor = self.qrBackgroundColor;
        self.imageView.backgroundColor = self.qrBackgroundColor;
        [self refreshQrCodeImage];
    }
    KUIKLY_SET_CSS_COMMON_PROP;
}

- (void)refreshQrCodeImage {
    if (self.content.length == 0 || CGRectIsEmpty(self.bounds)) {
        self.imageView.image = nil;
        return;
    }

    NSData *data = [self.content dataUsingEncoding:NSUTF8StringEncoding];
    CIFilter *qrFilter = [CIFilter filterWithName:@"CIQRCodeGenerator"];
    [qrFilter setValue:data forKey:@"inputMessage"];
    [qrFilter setValue:@"M" forKey:@"inputCorrectionLevel"];
    CIImage *qrImage = qrFilter.outputImage;
    if (!qrImage) {
        self.imageView.image = nil;
        return;
    }

    CIFilter *colorFilter = [CIFilter filterWithName:@"CIFalseColor"];
    [colorFilter setValue:qrImage forKey:kCIInputImageKey];
    [colorFilter setValue:[CIColor colorWithCGColor:self.foregroundColor.CGColor] forKey:@"inputColor0"];
    [colorFilter setValue:[CIColor colorWithCGColor:self.qrBackgroundColor.CGColor] forKey:@"inputColor1"];
    CIImage *coloredImage = colorFilter.outputImage;
    if (!coloredImage) {
        coloredImage = qrImage;
    }

    CGFloat screenScale = MAX(UIScreen.mainScreen.scale, 1.0);
    CGFloat imageWidth = CGRectGetWidth(coloredImage.extent);
    CGFloat imageHeight = CGRectGetHeight(coloredImage.extent);
    CGFloat scale = floor(MIN(
        self.bounds.size.width * screenScale / imageWidth,
        self.bounds.size.height * screenScale / imageHeight
    ));
    scale = MAX(scale, 1.0);
    CIImage *scaledImage = [coloredImage imageByApplyingTransform:CGAffineTransformMakeScale(scale, scale)];
    CGImageRef cgImage = [self.ciContext createCGImage:scaledImage fromRect:scaledImage.extent];
    if (!cgImage) {
        self.imageView.image = nil;
        return;
    }
    self.imageView.image = [UIImage imageWithCGImage:cgImage scale:screenScale orientation:UIImageOrientationUp];
    CGImageRelease(cgImage);
}

- (UIColor *)colorWithValue:(id)value defaultColor:(UIColor *)defaultColor {
    if (![value isKindOfClass:[NSString class]]) {
        return defaultColor;
    }
    NSString *hex = [(NSString *)value stringByTrimmingCharactersInSet:NSCharacterSet.whitespaceAndNewlineCharacterSet];
    if ([hex hasPrefix:@"#"]) {
        hex = [hex substringFromIndex:1];
    }
    if (hex.length != 6 && hex.length != 8) {
        return defaultColor;
    }

    unsigned int colorValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hex];
    if (![scanner scanHexInt:&colorValue]) {
        return defaultColor;
    }

    CGFloat alpha = 1.0;
    CGFloat red = 0.0;
    CGFloat green = 0.0;
    CGFloat blue = 0.0;
    if (hex.length == 8) {
        alpha = ((colorValue >> 24) & 0xFF) / 255.0;
        red = ((colorValue >> 16) & 0xFF) / 255.0;
        green = ((colorValue >> 8) & 0xFF) / 255.0;
        blue = (colorValue & 0xFF) / 255.0;
    } else {
        red = ((colorValue >> 16) & 0xFF) / 255.0;
        green = ((colorValue >> 8) & 0xFF) / 255.0;
        blue = (colorValue & 0xFF) / 255.0;
    }
    return [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
}

@end

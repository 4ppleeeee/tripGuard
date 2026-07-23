//
//  KRTextPostProcessorAdapter.m
//  iosApp
//
//  评论输入框表情后置处理适配器（iOS 端，OC 版）。
//  逻辑与原 Swift 版一致，迁移为 OC 以满足 SDK 端代码风格要求。
//

#import "KRTextPostProcessorAdapter.h"
#import "KREmojiTextAttachment.h"

NSString *const KRTextPostProcessorCommentInput = @"comment_input";

/// 统一日志 TAG，便于在 Xcode console / Console.app 中过滤排查表情后置处理链路。
static NSString *const kKRTextPostProcessorLogTag = @"[EmojiPP]";

@interface KRTextPostProcessorAdapter ()

/// position -> UIImage 的内存缓存，避免重复 IO 解码。
@property (nonatomic, strong) NSMutableDictionary<NSNumber *, UIImage *> *emojiImageCache;
/// 表情图片缓存的并发读写队列（barrier 写、并发读）。
@property (nonatomic, strong) dispatch_queue_t cacheQueue;

@end

@implementation KRTextPostProcessorAdapter

#pragma mark - Singleton

+ (instancetype)shared {
    static KRTextPostProcessorAdapter *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[KRTextPostProcessorAdapter alloc] init];
    });
    return instance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _emojiImageCache = [NSMutableDictionary dictionary];
        _cacheQueue = dispatch_queue_create("com.tencent.weishi.kr.emoji.cache",
                                            DISPATCH_QUEUE_CONCURRENT);
    }
    return self;
}

#pragma mark - Public

- (NSMutableAttributedString *)processWithAttributedString:(NSAttributedString *)attributedString
                                                      font:(UIFont *)font
                                                 processor:(NSString *)processor {
    NSMutableAttributedString *result =
        [[NSMutableAttributedString alloc] initWithAttributedString:attributedString];

    // 关键诊断点 1：回调是否被触发，以及 processor 实际值
    NSString *source = attributedString.string ?: @"";
    NSString *preview = source.length > 40 ? [source substringToIndex:40] : source;
    NSLog(@"%@ hit processor=\"%@\" len=%lu preview=\"%@\"",
          kKRTextPostProcessorLogTag,
          processor ?: @"",
          (unsigned long)attributedString.length,
          preview);

    if (![processor isEqualToString:KRTextPostProcessorCommentInput]) {
        NSLog(@"%@ skip: processor not match (expect \"%@\")",
              kKRTextPostProcessorLogTag, KRTextPostProcessorCommentInput);
        return result;
    }
    if (attributedString.length == 0) {
        return result;
    }

    NSRegularExpression *regex = [self.class emojiCodeRegex];
    if (!regex) {
        NSLog(@"%@ skip: regex nil", kKRTextPostProcessorLogTag);
        return result;
    }

    NSRange fullRange = NSMakeRange(0, source.length);
    NSArray<NSTextCheckingResult *> *matches =
        [regex matchesInString:source options:0 range:fullRange];
    // 关键诊断点 2：正则匹配命中数
    NSLog(@"%@ regex matched=%lu",
          kKRTextPostProcessorLogTag, (unsigned long)matches.count);
    if (matches.count == 0) {
        return result;
    }

    // 表情图标视觉尺寸：跟随字号略放大，并兜底最小尺寸，避免极小字号下表情看不清。
    CGFloat baseSize = font ? font.pointSize : 16.0;
    CGFloat emojiSize = MAX(baseSize * 1.2, 18.0);
    // 让附件相对基线略微下移，垂直视觉上居中于一行文字之间。
    // 取 descender 的一半（descender 为负值），是 UIKit 文档示例的常用做法。
    CGFloat attachmentY = (font ? font.descender : -3.0) * 0.5;

    NSString *sourceString = source;

    // 倒序替换避免位置偏移
    for (NSInteger i = (NSInteger)matches.count - 1; i >= 0; i--) {
        NSTextCheckingResult *match = matches[(NSUInteger)i];
        if (match.range.location == NSNotFound) {
            continue;
        }
        NSString *code = [sourceString substringWithRange:match.range];

        NSNumber *positionNumber = [self.class positionForCode:code];
        if (!positionNumber) {
            NSLog(@"%@ miss-mapping code=\"%@\"", kKRTextPostProcessorLogTag, code);
            continue;
        }

        UIImage *image = [self loadEmojiImageWithPosition:positionNumber.integerValue];
        if (!image) {
            NSLog(@"%@ miss-image code=\"%@\" pos=%@",
                  kKRTextPostProcessorLogTag, code, positionNumber);
            // 图片加载失败时保留原始短码，不做替换
            continue;
        }

        // 必须使用实现了 KRTextAttachmentStringProtocol 协议的子类，
        // 否则 KuiklyIOSRender SDK 会丢弃裸 NSTextAttachment，导致输入框看不到表情。
        KREmojiTextAttachment *attachment =
            [[KREmojiTextAttachment alloc] initWithImage:image originalText:code];
        attachment.bounds = CGRectMake(0, attachmentY, emojiSize, emojiSize);

        // 关键修复：UITextView 在布局 NSTextAttachment 时依赖该位置的 font 属性来计算行高，
        // 缺失 font 会导致 attachment 区域不绘制图像（看起来像"没插入图片"）。
        // 这里把替换前原 range 起点的所有富文本属性（含 font/foregroundColor）继承到
        // attachment string 上，确保 UITextView/UITextField 都能正确画出表情图。
        NSMutableAttributedString *attachmentString =
            [[NSMutableAttributedString alloc]
                initWithAttributedString:[NSAttributedString attributedStringWithAttachment:attachment]];
        NSDictionary<NSAttributedStringKey, id> *originAttrs =
            [result attributesAtIndex:match.range.location effectiveRange:NULL];
        if (originAttrs.count > 0) {
            [attachmentString addAttributes:originAttrs
                                      range:NSMakeRange(0, attachmentString.length)];
        }
        // 即使原本没有 font 属性，也强制兜底一个，避免 0 高度。
        if (![attachmentString attribute:NSFontAttributeName atIndex:0 effectiveRange:NULL]
            && font != nil) {
            [attachmentString addAttribute:NSFontAttributeName
                                     value:font
                                     range:NSMakeRange(0, attachmentString.length)];
        }
        [result replaceCharactersInRange:match.range withAttributedString:attachmentString];
    }
    NSLog(@"%@ done, finalLen=%lu",
          kKRTextPostProcessorLogTag, (unsigned long)result.length);
    return result;
}

#pragma mark - Image Loading

/// 根据表情 position 加载对应 png（emoji_fXXX.png），三位补零文件名。
/// 优先从内存缓存取，未命中则按多 Bundle 候选路径全局查找：
/// - Bundle.main 直接查
/// - 所有已加载 Bundle 中查（覆盖 KMM Framework / Compose Resources Bundle 等子 Bundle 场景）
/// - mainBundle 下常见的 Compose Resources 子目录
- (UIImage *)loadEmojiImageWithPosition:(NSInteger)position {
    UIImage *cached = [self readCacheForPosition:position];
    if (cached) {
        return cached;
    }

    NSString *fileName = [NSString stringWithFormat:@"emoji_f%03ld", (long)position];

    // 1) Bundle.main 直接查
    NSString *path = [[NSBundle mainBundle] pathForResource:fileName ofType:@"png"];

    // 2) 兜底扫描所有已加载 Bundle（含 KMM Framework / Compose Resources 等动态 Bundle）。
    if (!path) {
        NSMutableArray<NSBundle *> *bundles = [NSMutableArray array];
        [bundles addObjectsFromArray:[NSBundle allBundles]];
        [bundles addObjectsFromArray:[NSBundle allFrameworks]];
        for (NSBundle *bundle in bundles) {
            NSString *p = [bundle pathForResource:fileName ofType:@"png"];
            if (p.length > 0) {
                path = p;
                break;
            }
        }
    }

    // 3) 常见 Compose Resources 子目录命名（不同版本工具链可能产出不同的子目录名）。
    if (!path) {
        NSArray<NSString *> *composeSubDirs = @[
            @"compose-resources/drawable",
            @"compose-resources",
            @"drawable",
            @"composeResources/drawable",
            @"composeResources",
        ];
        for (NSString *subdir in composeSubDirs) {
            NSString *p = [[NSBundle mainBundle] pathForResource:fileName
                                                          ofType:@"png"
                                                     inDirectory:subdir];
            if (p.length > 0) {
                path = p;
                break;
            }
        }
    }

    if (path.length == 0) {
        NSLog(@"%@ bundle search miss: %@.png", kKRTextPostProcessorLogTag, fileName);
        return nil;
    }
    UIImage *image = [UIImage imageWithContentsOfFile:path];
    if (!image) {
        NSLog(@"%@ bundle search miss: %@.png", kKRTextPostProcessorLogTag, fileName);
        return nil;
    }
    NSLog(@"%@ bundle hit: %@.png at %@", kKRTextPostProcessorLogTag, fileName, path);
    [self writeCacheForPosition:position image:image];
    return image;
}

- (UIImage *)readCacheForPosition:(NSInteger)position {
    __block UIImage *image = nil;
    dispatch_sync(self.cacheQueue, ^{
        image = self.emojiImageCache[@(position)];
    });
    return image;
}

- (void)writeCacheForPosition:(NSInteger)position image:(UIImage *)image {
    if (!image) {
        return;
    }
    __weak typeof(self) weakSelf = self;
    dispatch_barrier_async(self.cacheQueue, ^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        strongSelf.emojiImageCache[@(position)] = image;
    });
}

#pragma mark - Symbol -> Position Mapping

/// 表情短码匹配正则：`[/xxx]`，xxx 内不含 `[` `]`。
+ (NSRegularExpression *)emojiCodeRegex {
    static NSRegularExpression *regex = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSError *error = nil;
        regex = [NSRegularExpression regularExpressionWithPattern:@"\\[/[^\\[\\]]+?\\]"
                                                          options:0
                                                            error:&error];
        if (error) {
            NSLog(@"%@ regex compile error: %@", kKRTextPostProcessorLogTag, error);
        }
    });
    return regex;
}

/// 通过表情短码（如 "[/呲牙]"）反查 position。
+ (NSNumber *)positionForCode:(NSString *)code {
    if (code.length == 0) {
        return nil;
    }
    return [self symbolToPosition][code];
}

/// `[/xxx]` -> position 映射表。
/// 数据源：wsFeeds/CommentEmojiLoader.kt 的 `ALL_EMO_FAST_SYMBOL` / `ALL_EMO_FAST_POSITION`。
/// 共 179 项，新增 / 调整表情时需同步更新此处与 Kotlin 端。
+ (NSDictionary<NSString *, NSNumber *> *)symbolToPosition {
    static NSDictionary<NSString *, NSNumber *> *map = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        map = @{
            @"[/微笑]": @23, @"[/撇嘴]": @40, @"[/色]": @19, @"[/发呆]": @43, @"[/得意]": @21,
            @"[/流泪]": @9,  @"[/害羞]": @20, @"[/闭嘴]": @106, @"[/睡]": @35, @"[/大哭]": @10,
            @"[/尴尬]": @25, @"[/发怒]": @24, @"[/调皮]": @1,  @"[/呲牙]": @0,  @"[/惊讶]": @33,
            @"[/难过]": @32, @"[/酷]": @12,   @"[/冷汗]": @27, @"[/抓狂]": @13, @"[/吐]": @22,
            @"[/偷笑]": @3,  @"[/可爱]": @18, @"[/白眼]": @30, @"[/傲慢]": @31, @"[/饥饿]": @81,
            @"[/困]": @82,   @"[/惊恐]": @26, @"[/流汗]": @2,  @"[/憨笑]": @37, @"[/装逼]": @50,
            @"[/奋斗]": @42, @"[/咒骂]": @83, @"[/疑问]": @34, @"[/嘘]": @11,   @"[/晕]": @49,
            @"[/折磨]": @84, @"[/衰]": @39,   @"[/骷髅]": @78, @"[/敲打]": @5,  @"[/再见]": @4,
            @"[/擦汗]": @6,  @"[/抠鼻]": @85, @"[/鼓掌]": @86, @"[/糗大了]": @87, @"[/坏笑]": @46,
            @"[/左哼哼]": @88, @"[/右哼哼]": @44, @"[/哈欠]": @89, @"[/鄙视]": @48, @"[/委屈]": @14,
            @"[/快哭了]": @90, @"[/阴险]": @41, @"[/亲亲]": @36, @"[/吓]": @91,   @"[/可怜]": @51,
            @"[/眨眼睛]": @143, @"[/笑哭]": @144, @"[/doge]": @145, @"[/泪奔]": @146, @"[/摊手]": @147,
            @"[/托腮]": @148, @"[/萌]": @149, @"[/斜眼笑]": @150, @"[/吐血]": @151, @"[/惊喜]": @152,
            @"[/无语]": @153, @"[/小纠结]": @154, @"[/戳自己]": @155, @"[/菜刀]": @17, @"[/西瓜]": @60,
            @"[/啤酒]": @61, @"[/篮球]": @92, @"[/乒乓]": @93, @"[/咖啡]": @66, @"[/饭]": @58,
            @"[/猪头]": @7,  @"[/玫瑰]": @8,  @"[/凋谢]": @57, @"[/示爱]": @29, @"[/爱心]": @28,
            @"[/心碎]": @74, @"[/蛋糕]": @59, @"[/闪电]": @80, @"[/炸弹]": @16, @"[/刀]": @70,
            @"[/足球]": @77, @"[/瓢虫]": @62, @"[/便便]": @15, @"[/月亮]": @68, @"[/太阳]": @75,
            @"[/礼物]": @76, @"[/拥抱]": @45, @"[/赞]": @52,   @"[/踩]": @53,   @"[/握手]": @54,
            @"[/胜利]": @55, @"[/抱拳]": @56, @"[/勾引]": @63, @"[/拳头]": @73, @"[/差劲]": @72,
            @"[/爱你]": @65, @"[/NO]": @94,   @"[/OK]": @64,   @"[/爱情]": @38, @"[/飞吻]": @47,
            @"[/跳跳]": @95, @"[/发抖]": @71, @"[/怄火]": @96, @"[/转圈]": @97, @"[/磕头]": @98,
            @"[/回头]": @99, @"[/跳绳]": @100, @"[/挥手]": @79, @"[/激动]": @101, @"[/街舞]": @102,
            @"[/献吻]": @103, @"[/左太极]": @104, @"[/右太极]": @105, @"[/双喜]": @108, @"[/鞭炮]": @109,
            @"[/灯笼]": @110, @"[/发财]": @111, @"[/K歌]": @112, @"[/购物]": @113, @"[/邮件]": @114,
            @"[/帅]": @115,   @"[/喝彩]": @116, @"[/祈祷]": @117, @"[/爆筋]": @118, @"[/棒棒糖]": @119,
            @"[/喝奶]": @120, @"[/下面]": @121, @"[/香蕉]": @122, @"[/飞机]": @123, @"[/开车]": @124,
            @"[/左车头]": @125, @"[/车厢]": @126, @"[/右车头]": @127, @"[/多云]": @128, @"[/下雨]": @129,
            @"[/钞票]": @130, @"[/熊猫]": @131, @"[/灯泡]": @132, @"[/风车]": @133, @"[/闹钟]": @134,
            @"[/打伞]": @135, @"[/彩球]": @136, @"[/钻戒]": @137, @"[/沙发]": @138, @"[/纸巾]": @139,
            @"[/药]": @140,   @"[/手枪]": @141, @"[/青蛙]": @142, @"[/茶]": @156,   @"[/蛋]": @157,
            @"[/红包]": @158, @"[/河蟹]": @159, @"[/羊驼]": @160, @"[/菊花]": @161, @"[/幽灵]": @162,
            @"[/大笑]": @163, @"[/不开心]": @164, @"[/冷漠]": @165, @"[/呃]": @166, @"[/好棒]": @167,
            @"[/拜托]": @168, @"[/点赞]": @169, @"[/无聊]": @170, @"[/托脸]": @171, @"[/吃]": @172,
            @"[/送花]": @173, @"[/害怕]": @174, @"[/花痴]": @175, @"[/小样儿]": @176, @"[/飙泪]": @177,
            @"[/我不看]": @178, @"[/栗子]": @179, @"[/肥皂]": @180, @"[/马赛克]": @181,
        };
    });
    return map;
}

@end

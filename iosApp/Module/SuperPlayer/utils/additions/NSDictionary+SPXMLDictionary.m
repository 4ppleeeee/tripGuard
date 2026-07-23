//
//  XMLDictionary.m
//
//  Version 1.0
//
//  Created by Nick Lockwood on 15/11/2010.
//  Copyright 2010 Charcoal Design. All rights reserved.
//
//  Get the latest version of XMLDictionary from either of these locations:
//
//  http://charcoaldesign.co.uk/source/cocoa#xmldictionary
//  https://github.com/demosthenese/xmldictionary
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  1. The origin of this software must not be misrepresented; you must not
//  claim that you wrote the original software. If you use this software
//  in a product, an acknowledgment in the product documentation would be
//  appreciated but is not required.
//
//  2. Altered source versions must be plainly marked as such, and must not be
//  misrepresented as being the original software.
//
//  3. This notice may not be removed or altered from any source distribution.
//

#import "NSDictionary+SPXMLDictionary.h"

@interface SPXMLDictionaryParser : NSObject <NSXMLParserDelegate> {
    NSMutableDictionary *root;
    NSMutableArray *stack;
    NSMutableString *text;
}

@property (nonatomic, retain) NSMutableDictionary *root;
@property (nonatomic, retain) NSMutableArray *stack;
@property (nonatomic, readonly) NSMutableDictionary *top;
@property (nonatomic, retain) NSMutableString *text;

+ (NSMutableDictionary *)spDictionaryWithXMLData:(NSData *)data;
+ (NSMutableDictionary *)spDictionaryWithXMLFile:(NSString *)path;
+ (NSString *)xmlStringForNode:(id)node withNodeName:(NSString *)nodeName;

@end

@implementation SPXMLDictionaryParser

@synthesize text;
@synthesize root;
@synthesize stack;

- (SPXMLDictionaryParser *)initWithXMLData:(NSData *)data {
    if ((self = [super init])) {
        NSXMLParser *parser = [[NSXMLParser alloc] initWithData:data];
        [parser setDelegate:self];
        [parser parse];
    }
    return self;
}

+ (NSMutableDictionary *)spDictionaryWithXMLData:(NSData *)data {
    return [[[SPXMLDictionaryParser alloc] initWithXMLData:data] root];
}

+ (NSMutableDictionary *)spDictionaryWithXMLFile:(NSString *)path {
    NSData *data = [NSData dataWithContentsOfFile:path];
    return [self spDictionaryWithXMLData:data];
}

+ (NSString *)xmlStringForNode:(id)node withNodeName:(NSString *)nodeName {
    if ([node isKindOfClass:[NSArray class]]) {
        NSMutableArray *nodes = [NSMutableArray arrayWithCapacity:[node count]];
        for (id individualNode in node) {
            [nodes addObject:[self xmlStringForNode:individualNode withNodeName:nodeName]];
        }
        return [nodes componentsJoinedByString:@"\n"];
    } else if ([node isKindOfClass:[NSDictionary class]]) {
        NSDictionary *attributes = [(NSDictionary *)node spAttributes];
        NSMutableString *attributeString = [NSMutableString string];
        for (NSString *key in [attributes allKeys]) {
            [attributeString appendFormat:@" %@=\"%@\"", [key spXmlEncodedString], [[attributes objectForKey:key] spXmlEncodedString]];
        }

        NSString *innerXML = [node spInnerXML];
        if ([innerXML length]) {
            return [NSString stringWithFormat:@"<%1$@%2$@>%3$@</%1$@>", nodeName, attributeString, innerXML];
        } else {
            return [NSString stringWithFormat:@"<%@%@/>", nodeName, attributeString];
        }
    } else {
        return [NSString stringWithFormat:@"<%1$@>%2$@</%1$@>", nodeName, [[node description] spXmlEncodedString]];
    }
}

- (NSMutableDictionary *)top {
    return [stack lastObject];
}

- (void)endText {
    if (TRIM_WHITE_SPACE) {
        self.text = (NSMutableString *)[text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    }
    if (text && ![text isEqualToString:@""] && [XML_TEXT_KEY length]) {
        id existing = [self.top objectForKey:XML_TEXT_KEY];
        if (existing) {
            if ([existing isKindOfClass:[NSMutableArray class]]) {
                [(NSMutableArray *)existing addObject:text];
            } else {
                [self.top setObject:[NSMutableArray arrayWithObjects:existing, text, nil] forKey:XML_TEXT_KEY];
            }
        } else {
            [self.top setObject:text forKey:XML_TEXT_KEY];
        }
    }
    self.text = nil;
}

- (void)addText:(NSString *)textString {
    if (!text) {
        self.text = [NSMutableString stringWithString:textString];
    } else {
        [text appendString:textString];
    }
}

- (void)parser:(NSXMLParser *)parser
    didStartElement:(NSString *)elementName
       namespaceURI:(NSString *)namespaceURI
      qualifiedName:(NSString *)qName
         attributes:(NSDictionary *)attributeDict {
    [self endText];

    NSMutableDictionary *node = [NSMutableDictionary dictionary];
    if ([XML_NAME_KEY length]) {
        [node setObject:elementName forKey:XML_NAME_KEY];
    }
    if ([attributeDict count]) {
        if ([XML_ATTRIBUTE_PREFIX length]) {
            for (NSString *key in [attributeDict allKeys]) {
                [node setObject:[attributeDict objectForKey:key] forKey:[XML_ATTRIBUTE_PREFIX stringByAppendingString:key]];
            }
        } else if ([XML_ATTRIBUTES_KEY length]) {
            [node setObject:attributeDict forKey:XML_ATTRIBUTES_KEY];
        } else {
            [node addEntriesFromDictionary:attributeDict];
        }
    }

    if (!self.top) {
        self.root = node;
        self.stack = [NSMutableArray arrayWithObject:node];
    } else {
        id existing = [self.top objectForKey:elementName];
        if (existing) {
            if ([existing isKindOfClass:[NSMutableArray class]]) {
                [(NSMutableArray *)existing addObject:node];
            } else {
                [self.top setObject:[NSMutableArray arrayWithObjects:existing, node, nil] forKey:elementName];
            }
        } else {
            [self.top setObject:node forKey:elementName];
        }
        [stack addObject:node];
    }
}

- (NSString *)nameForNode:(NSDictionary *)node inDictionary:(NSDictionary *)dict {
    if (node.spNodeName) {
        return node.spNodeName;
    } else {
        for (NSString *name in dict) {
            id object = [dict objectForKey:name];
            if (object == node) {
                return name;
            } else if ([object isKindOfClass:[NSArray class]]) {
                if ([(NSArray *)object containsObject:node]) {
                    return name;
                }
            }
        }
    }
    return nil;
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    [self endText];
    if (COLLAPSE_TEXT_NODES && !self.top.spAttributes && !self.top.spChildNodes && !self.top.spComments && self.top.spInnerText) {
        NSDictionary *node = self.top;
        [stack removeLastObject];
        NSString *nodeName = [self nameForNode:node inDictionary:self.top];
        if (nodeName) {
            id parentNode = [self.top objectForKey:nodeName];
            if ([parentNode isKindOfClass:[NSMutableArray class]]) {
                [parentNode replaceObjectAtIndex:[parentNode count] - 1 withObject:node.spInnerText];
            } else {
                [self.top setObject:node.spInnerText forKey:nodeName];
            }
        }
    } else {
        [stack removeLastObject];
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    [self addText:string];
}

- (void)parser:(NSXMLParser *)parser foundComment:(NSString *)comment {
    if ([XML_COMMENTS_KEY length]) {
        NSMutableArray *comments = [self.top objectForKey:XML_COMMENTS_KEY];
        if (!comments) {
            comments = [NSMutableArray arrayWithObject:comment];
            [self.top setObject:comments forKey:XML_COMMENTS_KEY];
        } else {
            [comments addObject:comment];
        }
    }
}

- (void)dealloc {
}

@end

@implementation NSDictionary (SPXMLDictionary)

+ (NSDictionary *)spDictionaryWithXMLData:(NSData *)data {
    return [SPXMLDictionaryParser spDictionaryWithXMLData:data];
}

+ (NSDictionary *)spDictionaryWithXMLString:(NSString *)string {
    NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
    return [SPXMLDictionaryParser spDictionaryWithXMLData:data];
}

+ (NSDictionary *)spDictionaryWithXMLFile:(NSString *)path {
    return [SPXMLDictionaryParser spDictionaryWithXMLFile:path];
}

- (id)spAttributeForKey:(NSString *)key {
    return [[self spAttributes] objectForKey:key];
}

- (NSDictionary *)spAttributes {
    NSDictionary *attributes = [self objectForKey:XML_ATTRIBUTES_KEY];
    if (attributes) {
        return [attributes count] ? attributes : nil;
    } else if ([XML_ATTRIBUTE_PREFIX length]) {
        NSMutableDictionary *filteredDict = [NSMutableDictionary dictionaryWithDictionary:self];
        [filteredDict removeObjectsForKeys:[NSArray arrayWithObjects:XML_COMMENTS_KEY, XML_TEXT_KEY, XML_NAME_KEY, nil]];
        for (NSString *key in [filteredDict allKeys]) {
            [filteredDict removeObjectForKey:key];
            if ([key hasPrefix:XML_ATTRIBUTE_PREFIX]) {
                [filteredDict setObject:[self objectForKey:key] forKey:[key substringFromIndex:[XML_ATTRIBUTE_PREFIX length]]];
            }
        }
        return [filteredDict count] ? filteredDict : nil;
    }
    return nil;
}

- (NSDictionary *)spChildNodes {
    NSMutableDictionary *filteredDict = [NSMutableDictionary dictionaryWithDictionary:self];
    [filteredDict removeObjectsForKeys:[NSArray arrayWithObjects:XML_ATTRIBUTES_KEY, XML_COMMENTS_KEY, XML_TEXT_KEY, XML_NAME_KEY, nil]];
    if ([XML_ATTRIBUTE_PREFIX length]) {
        for (NSString *key in [filteredDict allKeys]) {
            if ([key hasPrefix:XML_ATTRIBUTE_PREFIX]) {
                [filteredDict removeObjectForKey:key];
            }
        }
    }
    return [filteredDict count] ? filteredDict : nil;
}

- (NSArray *)spComments {
    return [self objectForKey:XML_COMMENTS_KEY];
}

- (NSString *)spNodeName {
    return [self objectForKey:XML_NAME_KEY];
}

- (id)spInnerText {
    id text = [self objectForKey:XML_TEXT_KEY];
    if ([text isKindOfClass:[NSArray class]]) {
        return [text componentsJoinedByString:@"\n"];
    } else {
        return text;
    }
}

- (NSString *)spInnerXML {
    NSMutableArray *nodes = [NSMutableArray array];

    for (NSString *comment in [self spComments]) {
        [nodes addObject:[NSString stringWithFormat:@"<!--%@-->", [comment spXmlEncodedString]]];
    }

    NSDictionary *childNodes = [self spChildNodes];
    for (NSString *key in childNodes) {
        [nodes addObject:[SPXMLDictionaryParser xmlStringForNode:[childNodes objectForKey:key] withNodeName:key]];
    }

    NSString *text = [self spInnerText];
    if (text) {
        [nodes addObject:[text spXmlEncodedString]];
    }

    return [nodes componentsJoinedByString:@"\n"];
}

- (NSString *)spXmlString {
    return [SPXMLDictionaryParser xmlStringForNode:self withNodeName:[self spNodeName] ?: @"root"];
}

@end

@implementation NSString (SPXMLDictionary)

- (NSString *)spXmlEncodedString {
    return [[[[self stringByReplacingOccurrencesOfString:@"&" withString:@"&amp;"] stringByReplacingOccurrencesOfString:@"<" withString:@"&lt;"]
        stringByReplacingOccurrencesOfString:@">"
                                  withString:@"&gt;"] stringByReplacingOccurrencesOfString:@"\""
                                                                                withString:@"&quot;"];
}

@end

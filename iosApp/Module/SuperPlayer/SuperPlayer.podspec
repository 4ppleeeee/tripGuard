#
#  Be sure to run `pod spec lint ThumbPlayer.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see https://guides.cocoapods.org/syntax/podspec.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |s|

  # ―――  Spec Metadata  ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  These will help people to find your library, and whilst it
  #  can feel like a chore to fill in it's definitely to your advantage. The
  #  summary should be tweet-length, and the description more in depth.
  #

  s.name         = 'SuperPlayer'
  s.version          = '1.5.0.1.133'
  s.summary      = '中台播放器落地手Q iOS、看点视频APP等业务场景'

  # This description is used to generate tags and improve search results.
  #   * Think: What does it do? Why did you write it? What is the focus?
  #   * Try to keep it short, snappy and to the point.
  #   * Write the description between the DESC delimiters below.
  #   * Finally, don't worry about the indent, CocoaPods strips it!
  s.description  = <<-DESC
  SuperPlayer是基于中台播放器ThumbPlayer打造的终端视频播放一体化解决方案，并针对实际业务场景，特别是信息流业务进行了定制化及优化，支持业务快速接入。
目前SuperPlayer已接入腾讯视频源防盗链模块，支持包括MP4、HLS等格式的点播及直播防盗链请求。播放能力方面，在ThumbPlayer现有播放能力基础上，封装了预下载任务调度等常用业务接口，同时集成了自研的解码器复用等秒开优化能力。质量监控方面，接入后即可直接使用中台播放质量监控报表查看数据，同时后续将逐步建设更加精细化的播放质量监控体系。
SuperPlayer目前已接入手Q及看点视频等APP，经过多个版本迭代，性能稳定。
                   DESC

  s.homepage     = "https://git.code.oa.com/MicrovisionComponents/SuperPlayer.git"
  # s.screenshots  = "www.example.com/screenshots_1.gif", "www.example.com/screenshots_2.gif"


  # ―――  Spec License  ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  Licensing your code is important. See https://choosealicense.com for more info.
  #  CocoaPods will detect a license file if there is a named LICENSE*
  #  Popular ones are 'MIT', 'BSD' and 'Apache License, Version 2.0'.
  #

  # s.license      = "MIT (example)"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }


  # ――― Author Metadata  ――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  Specify the authors of the library, with email addresses. Email addresses
  #  of the authors are extracted from the SCM log. E.g. $ git log. CocoaPods also
  #  accepts just a name if you'd rather not provide an email address.
  #
  #  Specify a social_media_url where others can refer to, for example a twitter
  #  profile URL.
  #

  s.author             = { "ethanyxliu" => "ethanyxliu@tencent.com" }
  # Or just: spec.author    = "ethanyxliu"
  # s.authors            = { "ethanyxliu" => "ethanyxliu@tencent.com" }
  # s.social_media_url   = "https://twitter.com/ethanyxliu"

  # ――― Platform Specifics ――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  If this Pod runs only on iOS or OS X, then specify the platform and
  #  the deployment target. You can optionally include the target after the platform.
  #

  # s.platform     = :ios
  s.platform     = :ios, "9.0"

  #  When using multiple platforms
  # s.ios.deployment_target = "5.0"
  # s.osx.deployment_target = "10.7"
  # s.watchos.deployment_target = "2.0"
  # s.tvos.deployment_target = "9.0"


  # ――― Source Location ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  Specify the location from where the source should be retrieved.
  #  Supports git, hg, bzr, svn and HTTP.
  #

  s.source       = { :git => "http://git.code.oa.com/MicrovisionComponents/SuperPlayer.git", :tag => s.version.to_s }


  # ――― Source Code ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  CocoaPods is smart about how it includes source code. For source files
  #  giving a folder will include any swift, h, m, mm, c & cpp files.
  #  For header files it will include any header in the folder.
  #  Not including the public_header_files will make all headers public.
  #

  s.source_files = "api/**/*.h",
                      "api/**/*.m", 
                      "cgi/**/*.h", 
                      "cgi/**/*.m", 
                      "common/**/*.h", 
                      "common/**/*.m", 
                      "common/**/*.mm", 
                      "common/**/*.c",  
                      "dlna/**/*.h", 
                      "dlna/**/*.m", 
                      "player/**/*.h", 
                      "player/**/*.m", 
                      "player/**/*.mm", 
                      "plugin/**/*.h", 
                      "plugin/**/*.m", 
                      "preload/**/*.h", 
                      "preload/**/*.m", 
                      "utils/**/*.h", 
                      "utils/**/*.m", 
                      "view/**/*.h", 
                      "view/**/*.m"

  # s.exclude_files = "Class/Exclude"

  s.public_header_files = "api/**/*.h",
                             "cgi/**/*.h", 
                             "common/**/*.h", 
                             "dlna/**/*.h", 
                             "player/**/*.h", 
                             "plugin/**/*.h", 
                             "preload/**/*.h", 
                             "utils/**/*.h", 
                             "view/**/*.h"


  # ――― Resources ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  A list of resources included with the Pod. These are copied into the
  #  target bundle with a build phase script. Anything else will be cleaned.
  #  You can preserve files from being cleaned, please don't preserve
  #  non-essential files like tests, examples and documentation.
  #

  # s.resource  = "icon.png"
  # s.resources = "Resources/*.png"

  # s.preserve_paths = "FilesToSave", "MoreFilesToSave"


  # ――― Project Linking ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  Link your library with frameworks, or libraries. Libraries do not include
  #  the lib prefix of their name.
  #

  # s.framework  = "SomeFramework"
  s.frameworks = "Foundation", "UIKit", "CoreMedia", "SystemConfiguration", "MediaToolbox", "VideoToolbox", "AudioToolbox"

  # s.library   = "iconv"
  s.libraries = "xml2", "resolv", "z", "bz2", "iconv", "c++"

  # s.vendored_libraries = "common/**/*.a"

  # ――― Project Settings ――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  If your library depends on compiler flags you can set them in the xcconfig hash
  #  where they will only apply to your library. If you depend on other Podspecs
  #  you can include multiple dependencies to ensure it works.
  
  s.prefix_header_file = 'SuperPlayerPrefix.pch'

  s.pod_target_xcconfig = { "VALID_ARCHS" => "arm64 armv7 x86_64", 'OTHER_LDFLAGS' => '-lObjC', 'OTHER_CFLAGS' => '-DSPPLAYER_NO_CONNECT_SDK -DSPPLAYER_NO_ASI -DSPPLAYER_NO_AUTH'}
  
  s.static_framework = true

#  s.dependency "DownloadProxyFramework", '~>0.0.16.205'
#  s.dependency "ThumbPlayer"
  
  s.dependency 'DownloadProxyFramework'
  s.dependency 'ThumbPlayer'
  s.dependency "vsCKey", '4.0.35'
#  s.dependency "BeaconAPI_BaseTVK"
  s.dependency "WSYYKit"

end

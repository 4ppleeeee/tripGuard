#
#  Be sure to run `pod spec lint QQOpenSDK.podspec.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see https://guides.cocoapods.org/syntax/podspec.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |spec|

  # ―――  Spec Metadata  ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  These will help people to find your library, and whilst it
  #  can feel like a chore to fill in it's definitely to your advantage. The
  #  summary should be tweet-length, and the description more in depth.
  #

  spec.name         = "QQOpenSDK"
  spec.version      = "3.6.20"
  spec.summary      = "QQ share&login SDK for Wesee"

  # This description is used to generate tags and improve search results.
  #   * Think: What does it do? Why did you write it? What is the focus?
  #   * Try to keep it short, snappy and to the point.
  #   * Write the description between the DESC delimiters below.
  #   * Finally, don't worry about the indent, CocoaPods strips it!
  spec.description  = <<-DESC
                      腾讯QQ互联SDK
                   DESC

  spec.homepage     = "https://wiki.connect.qq.com/sdk%e4%b8%8b%e8%bd%bd"
  spec.author       = { "weishi" => "weishi@tencent.com" }
  spec.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  spec.source       = { :git => "https://git.woa.com/weishi/ios/QQOpenSDK.git", :tag => "v#{spec.version}" }
  spec.platform     = :ios

  spec.ios.deployment_target = "13.0"

  spec.resource = 'TencentOpenApi_IOS_Bundle.bundle'
  spec.vendored_frameworks  = "TencentOpenAPI.framework"
  spec.frameworks = "SystemConfiguration", "WebKit"
  spec.xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }

end

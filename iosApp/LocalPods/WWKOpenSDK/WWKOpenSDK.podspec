Pod::Spec.new do |s|
  s.name         = 'WWKOpenSDK'
  s.version      = '2.0.5'
  s.summary      = 'WeCom (Enterprise WeChat) Open SDK for iOS'
  s.description  = 'Enterprise WeChat Open SDK for sharing and authentication. Downloaded from https://developer.work.weixin.qq.com/document/path/91196'
  s.homepage     = 'https://developer.work.weixin.qq.com'
  s.license      = { :type => 'Copyright', :text => 'Copyright Tencent' }
  s.author       = 'Tencent WeCom'
  s.platform     = :ios, '12.0'
  s.source       = { :path => '.' }

  s.source_files = 'Frameworks/*.h'
  s.public_header_files = 'Frameworks/*.h'
  s.vendored_libraries = 'Frameworks/libWXWorkApi.a'
  s.frameworks   = 'SystemConfiguration', 'Security'
  s.requires_arc = true
end

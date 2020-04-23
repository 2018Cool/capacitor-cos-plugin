
  Pod::Spec.new do |s|
    s.name = 'CapacitorCosPlugin'
    s.version = '1.0.0'
    s.summary = '腾讯cos capacitor 文件上传插件'
    s.license = 'MIT'
    s.homepage = '?'
    s.author = 'zwc'
    s.source = { :git => '?', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
    s.dependency 'QCloudCOSXML/Transfer'
    s.static_framework = true
  end
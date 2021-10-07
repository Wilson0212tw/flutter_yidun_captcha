class YidunCaptchaConfig {
  final String captchaId;
  final String mode;
  final int timeout;
  final String languageType;
  final bool hideCloseButton;
  final String loadingText;

  YidunCaptchaConfig({
    this.captchaId,
    this.mode,
    this.timeout,
    this.languageType,
    this.loadingText,
    this.hideCloseButton,
  });

  Map<String, dynamic> toJson() {
    Map<String, dynamic> jsonObject = Map<String, dynamic>();
    if (captchaId != null) jsonObject.putIfAbsent("captchaId", () => captchaId);
    if (mode != null) jsonObject.putIfAbsent("mode", () => mode);
    if (timeout != null) jsonObject.putIfAbsent("timeout", () => timeout);
    if (languageType != null)
      jsonObject.putIfAbsent("languageType", () => languageType);
    if (hideCloseButton != null)
      jsonObject.putIfAbsent("hideCloseButton", () => hideCloseButton);
    if (loadingText != null)
      jsonObject.putIfAbsent("loadingText", () => loadingText);

    return jsonObject;
  }
}

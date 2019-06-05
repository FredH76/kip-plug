var BackgroundVideo = function () {
};


/**************************************************************************
 *  COOL Method  
 *************************************************************************/
BackgroundVideo.prototype.coolMethod = function (arg0, success, error) {
    cordova.exec(success, error, 'BackgroundVideo', 'coolMethod', [arg0]);
};
BackgroundVideo.prototype.hasCamera = function (success, error) {
    cordova.exec(success, error, 'BackgroundVideo', 'hasCamera');
};
BackgroundVideo.prototype.setQuality = function (arg0, success, error) {
    cordova.exec(success, error, 'BackgroundVideo', 'setQuality', [arg0]);
};
BackgroundVideo.prototype.selectCamera = function (arg0, success, error) {
    cordova.exec(success, error, 'BackgroundVideo', 'selectCamera', [arg0]);
};
BackgroundVideo.prototype.startVideoRecord = function (arg0, success, error) {
    cordova.exec(success, error, 'BackgroundVideo', 'startVideoRecord', [arg0]);
};
BackgroundVideo.prototype.stopVideoRecord = function (success, error) {
    cordova.exec(success, error, 'BackgroundVideo', 'stopVideoRecord');
};

// export a new BackgroundVideo instance
var backgroundVideo = new BackgroundVideo();
module.exports = backgroundVideo;
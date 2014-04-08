// Helper function to make CORS AJAX requests to cow-server.
// Path is the section of the url after cow-server/. 
// Ex. if url is http://scout2:8080/cow-server/tasks?candidate=brosenberg, 
//  path will be tasks?candidate=brosenberg. The beginning of the url is defined in config.json
// httpMethod is optional and defaults to get
// data is optional and defaults to nothing. data is what will be in the body of the ajax request
// returns the jqXHR object so the deferred methods like done, success, fail, etc can be called
var cowRequest = function(path, httpMethod, data) {
    
    var defaultAjaxParams = {
        url: config.cowServerHost + path,
        dataType: "json",
        xhrFields: {
            withCredentials: true
        }
    };    
    var optionalParams = {
        data: data,
        type: httpMethod
    };
    
    var ajaxParams = $.extend({ }, defaultAjaxParams, optionalParams);    
    
    return $.ajax(ajaxParams);
};

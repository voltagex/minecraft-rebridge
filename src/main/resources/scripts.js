function renderRoutes(targetElement) {
    var target = document.getElementById(targetElement);
    qwest.get('/Debug/Routes')
        .then(function (xhr, response) {
            var routes = JSON.parse(response);

            var result = [];
            _.each(_.keys(routes), function (key) {
                _.each(routes[key], function (innerItem) {
                    result.push("/" + key + "/" + innerItem)
                });
            })

            var html = "";
            _.each(result, function (link) {
                html += '<a href="'+ link +'">' + link + '</a><br>';
            })

            target.innerHTML = html;

        });
}

function setClientSetting(key, value) {
    var postData = {}
    postData['Name'] = key;
    postData['Value'] = value;
    qwest.post('/Debug/GameSettings', postData, {dataType: "json"});
}
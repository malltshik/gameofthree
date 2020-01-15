var app = angular.module("app", []);

// app.service("PlayerFactory", ["$rootScope", function ($rootScope) {
//     this.updatePlayers = function (list) {
//         $rootScope.$apply(function () {
//             $rootScope.players = list;
//         })
//     }
// }]);

app.run(['$rootScope', function ($rootScope) {
    $rootScope.noActiveGame = true;
    $rootScope.socket = new WebSocket('ws://localhost:8080/api/websocket');
    $rootScope.ws = Stomp.over($rootScope.socket);
    $rootScope.ws.connect({}, function (frame) {
        $rootScope.ws.subscribe("/topic/hub", function (message) {
            $rootScope.$apply(function () {
                $rootScope.players = JSON.parse(message.body);
            })
        });
        $rootScope.ws.subscribe("/user/queue/reply", function (message) {
            console.debug("Message " + message.body);
        });
        $rootScope.ws.subscribe("/user/queue/errors", function (message) {
            console.error("Error " + message.body);
        });
    }, function (error) {
        console.error("STOMP error " + error);
    });
}]);
var app = angular.module("app", []);
var gameModal = $('#gameModal');

app.run(['$rootScope', function ($rootScope) {
    $rootScope.players = [];
    $rootScope.user = {
        status: 0,
        id: null
    };
    $rootScope.opponents = [];
    ws($rootScope);
}]);

app.controller("HubCtrl", ["$rootScope", "$scope", function ($rootScope, $scope){

    this.selectOpponent = function (id) {
        $rootScope.user.status = 1;
        $rootScope.game = {player1: $rootScope.user.id, player2: id};
        $rootScope.ws.send("/app/start", {}, id);
        gameModal.modal({backdrop: 'static', keyboard: false})
    };



    this.updateGameLog = function (entry) {
        console.log(entry);
    }


}]);

app.controller("GameCtrl", ["$rootScope", "$scope", function ($rootScope, $scope){
    this.leave = function () {
        gameModal.modal('hide');
        $rootScope.user.status = 0;
        $rootScope.game = null;
    };

    $rootScope.receiveInvite = function(invite) {
        $rootScope.$apply(function () {
            $rootScope.opponents.push(invite.opponent);
        });
        $rootScope.ws.subscribe(`/user/queue/game/${invite.gameId}`, function (message) {
            updateGameLog(JSON.parse(message.body))
        });
    };

    this.updateGameLog = function (entry) {
        console.log(entry);
    }

}]);

function ws($rs) {
    $rs.socket = new WebSocket('ws://localhost:8080/api/websocket');
    $rs.ws = Stomp.over($rs.socket);
    $rs.ws.connect({}, function (frame) {
        $rs.ws.subscribe("/topic/hub", function (message) {
            $rs.$apply(function () {
                if(!$rs.user.id) {
                    $rs.user.id = message.headers["message-id"].substring(0, 36);
                }
                $rs.players = JSON.parse(message.body);
            });
        });
        $rs.ws.subscribe("/user/queue/game", function (message) {
            $rs.receiveInvite(JSON.parse(message.body))
        });
        $rs.ws.subscribe("/user/queue/errors", function (message) {
            console.error("Error " + message.body);
        });
    }, function (error) {
        console.error("STOMP error " + error);
    });
}
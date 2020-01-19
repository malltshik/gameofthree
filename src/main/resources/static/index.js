var app = angular.module("app", []);
var gameModal = $('#gameModal');

app.run(['$rootScope', function ($rootScope) {
    $rootScope.players = [];
    $rootScope.user = {status: 0, id: null};
    $rootScope.game = null;
    $rootScope.invites = {};
    $rootScope.hasInvites = function () {
        return Object.keys($rootScope.invites).length > 0;
    };
    ws($rootScope);
}]);

app.controller("HubCtrl", ["$rootScope", "$scope", function ($rootScope, $scope) {

    this.openChallenge = function (id) {
        $rootScope.user.status = 1;
        $rootScope.ws.send("/app/challenge/open", {}, id);
    };

    this.acceptChallenge = function (game) {
        $rootScope.user.status = 1;
        delete $rootScope.invites[game.id];
        $rootScope.ws.send("/app/challenge/accept", {}, JSON.stringify(game));
    };

    $rootScope.receiveChallengeClosed = function (game) {
        $rootScope.$apply(function () {
            delete $rootScope.invites[game.id];
        });
    };

}]);

app.controller("GameCtrl", ["$rootScope", "$scope", '$location', '$anchorScroll', "$timeout",
    function ($rootScope, $scope, $location, $anchorScroll, $timeout) {

        $scope.autoplay = false;

        $scope.autoplayToggle = function () {
            $scope.autoplay = !$scope.autoplay;
            $scope.botMove();
        };

        $scope.closeChallenge = function () {
            gameModal.modal('hide');
            $rootScope.ws.send("/app/challenge/closeChallenge", {}, JSON.stringify($rootScope.openedChallenge));
            $rootScope.user.status = 0;
            $rootScope.openedChallenge = null;
        };

        $rootScope.receiveGame = function (game) {
            if (game.accepted) {
                $scope.joinGame(game);
                if ($rootScope.user.id === game.player2) {
                    gameModal.modal({backdrop: 'static', keyboard: false});
                }
                return;
            }
            if (game.player1 === $rootScope.user.id) {
                gameModal.modal({backdrop: 'static', keyboard: false});
                $scope.$apply(function () {
                    $rootScope.openedChallenge = game;
                });
            } else {
                $rootScope.$apply(function () {
                    $rootScope.invites[game.id] = game;
                });
            }
        };

        $scope.joinGame = function (game) {
            $rootScope.$apply(function () {
                $rootScope.game = game;
                $rootScope.openedChallenge = null;
            });
            $rootScope.gameChannel = $rootScope.ws.subscribe(`/user/queue/game/${game.id}/moves`, function (message) {
                $rootScope.$apply(function () {
                    $rootScope.game = JSON.parse(message.body);
                    let move = $rootScope.game.moves[$rootScope.game.moves.length - 1];
                    if ($rootScope.game && !$rootScope.game.winner && move.author !== $rootScope.user.id && $scope.autoplay) {
                        $scope.botMove()
                    }
                    $location.hash(`move-${$rootScope.game.moves.length - 1}`);
                    $anchorScroll();
                });
            });
        };

        $rootScope.receiveOpponentLeft = function (game) {
            if ($rootScope.game != null) {
                $rootScope.$apply(function () {
                    $rootScope.game.winner = $rootScope.user.id;
                    $rootScope.error = "Opponent has left the game :("
                })
            } else {
                $rootScope.$apply(function () {
                    delete $rootScope.invites[game.id];
                });
            }
        };

        $scope.leaveGame = function (game) {
            if($rootScope.gameChannel) {
                $rootScope.gameChannel.unsubscribe();
                $rootScope.gameChannel = null;
            }
            gameModal.modal("hide");
            $rootScope.user.status = 0;
            $rootScope.game = null;
            $rootScope.error = null;
            $scope.autoplay = false;
        };

        $scope.hasMove = function () {
            return $rootScope.game.currentPlayer === $rootScope.user.id && !$rootScope.autoplay;
        };

        $scope.isActiveMove = function (player) {
            return player === $rootScope.game.currentPlayer;
        };

        $scope.move = function (move) {
            $rootScope.error = null;
            $rootScope.ws.send(`/app/game/${$rootScope.game.id}/move`, {}, JSON.stringify(move));
        };

        $scope.botMove = function () {
            if ($scope.isActiveMove($rootScope.user.id)) {
                let len = $rootScope.game.moves.length;
                let num = len === 0 ? $rootScope.game.number : $rootScope.game.moves[len - 1].current;
                if ((num) % 3 === 0) {
                    $scope.move(0);
                }
                if ((num + 1) % 3 === 0) {
                    $scope.move(1);
                    return;
                }
                if ((num - 1) % 3 === 0) {
                    $scope.move(-1);
                }
            }
        }
    }]);

function ws($rs) {
    $rs.socket = new WebSocket('ws://localhost:8080/api/websocket');
    $rs.ws = Stomp.over($rs.socket);
    $rs.ws.connect({}, function (frame) {
        $rs.ws.subscribe("/topic/hub", function (message) {
            $rs.$apply(function () {
                if (!$rs.user.id) {
                    $rs.user.id = message.headers["message-id"].substring(0, 36);
                }
                $rs.players = JSON.parse(message.body);
            });
        });
        $rs.ws.subscribe("/user/queue/game", function (message) {
            $rs.receiveGame(JSON.parse(message.body))
        });
        $rs.ws.subscribe("/user/queue/challengeClosed", function (message) {
            $rs.receiveChallengeClosed(JSON.parse(message.body))
        });
        $rs.ws.subscribe("/user/queue/opponentLeft", function (message) {
            $rs.receiveOpponentLeft(JSON.parse(message.body))
        });
        $rs.ws.subscribe("/user/queue/errors", function (message) {
            $rs.$apply(function () {
                $rs.error = message.body;
            });
        });
    }, function (error) {
        console.error("STOMP error " + error);
    });
    var websocket = $rs.ws;
}
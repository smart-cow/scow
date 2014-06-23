var cowConfig = {
    cowServerHost: "http://scout3:8080/cow-server/",
    //cowServerHost: "http://scout3:8080/cow-server/",
//    cowServerHost: "http://dfmb2:8080/cow-server/",
    //cowServerHost: "http://localhost:8080/cow-server/",
//    cowServerHost: "http://mm180389-pc.mitre.org:8080/cow-server/",
    amqpUrl: "http://scout3:15674/stomp",
//    amqpUrl: "http://dfmb2:15674/stomp",
//    amqpUrl: "http://mm180389-pc.mitre.org:15674/stomp",
    amqpExchange: "/exchange/amq.topic/",
    amqpConnectTimeout: 5 * 1000
};

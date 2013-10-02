var vertx = require('vertx')

var server = vertx.createHttpServer()

server.requestHandler(function(req) {
    req.response.sendFile('resources/' + 
                          (req.path() == '/' ? 'index.html' : req.path()))
})

vertx.
    createSockJSServer(server).
    bridge({prefix: '/eventbus'}, [{}], [{}])

server.listen(8080, 'localhost')

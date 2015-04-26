#To do
## Routing
* probably similar to Microsoft's WebAPI routing - can I have attributes on methods in Java?

`GET /api/status`
`GET /api/player/{player}/{attribute}`
`GET /api/world/{world}/{attribute}`
``

In progress. I should clean this up and split it into multiple files.


## Controller discovery
Controllers should be discovered once at startup, not once per request

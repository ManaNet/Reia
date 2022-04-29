[![Mana](https://cdn.manabot.fun/images/test.png)](https://manabot.fun)
<div align="center">
from Mana
</div>

#
Reia is a simple wrapper around Lettuce to enable easy usage of its Redis Pubsub client. This library is only intended to be used 
for sending messages and then receiving a response, sending a simple message or producing responses to requests. 

## Usage

### Prerequisites
To use Reia, you need to have a Reia instance which can be created by using the method:
```kotlin
val reia = Reia.new(ReidsClient.create(...), {
    it.callbackPrefix = "Recommended-To-Change-This"
})

// producers add here.

reia.listen() // only use this once you have attached all the producers.
```

### Producers
You can then create a producer by creating a new class that implements the `ReiaProducer` class:
```kotlin
object HelloWorldProducer: ReiaProducer {

    fun onMessage(channel: String, message: ReiaMessage): ReiaGeneralMessage? {
        if (message.json()!!.optString("node") != null && message.json()!!.getString("node") === 0) {
            return ReiaPublisherMessage("Hello World", message.callback())
        }
        
        return null
    }
    
}
```

And once the class is created, you can then attach it to your Reia instance by using the `producer(channel: String, listener: ReiaProducer)` method:
```kotlin
reia.producer("hello.world", HelloWorldProducer)
```

> :yellow_circle: Reia appends `.requests` and `.consumer` to channels depending on what event is being done. For requests to producers, Reia will listen 
> and propogate to producers any messages received on `.requests` and for responses, Reia will listen and propogate to their specified callbacks any messages 
> received on `.consumer` that matches the callback.
> 
> In this example, Reia will propogate any messages received on `hello.world.requests` onto our `HelloWorldProducer` and will send any responses to the 
> `hello.world.consumer` channel.

### Requesting

You can make a simple request with Reia by using the `send` methods of Reia which expects the following:
- `ReiaGeneralMessage`: The message to send to the channel.
- `Duration`: The time to live of the message, this is by default 1-minute. It's recommeneded to go by 10 second increments since the 
cleaner cleans at a fixed rate of every ten seconds.
- `Channel`: The channel to send the message towards, this is auto-appended with `.requests`

A sample of sending a simple hello world request to our `HelloWorldProducer` would be:
```kotlin
reia.send("hello.world", ReiaGeneralMessage(JSONObject().put("node", 0))).thenAccept {
    println(it.full().get("message"))
}
```
```shell
Hello World
```

## Installation
You can install Reia from Jitpack with the following installation methods for Maven and Gradle:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
```groovy
dependencies {
    implementation 'pw.mihou:Reia:Tag'
}
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependency>
    <groupId>pw.mihou</groupId>
    <artifactId>Reia</artifactId>
    <version>Tag</version>
</dependency>
```
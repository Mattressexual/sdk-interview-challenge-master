# Pison Android Skeleton Project

You can change/add to this project as you see fit, but we recommend that you get it to build and run as-is before trying to make any changes. **This is still a work-in-progress on our end, and we want to make sure you don't waste time debugging _our_ issues!**

Some things to note:
* **Our SDK has already been added as a dependency to this project**. It's in the `libs` folder.
* We have pulled in a number of other dependencies, via the application `build.gradle`'s `dependencies` block. Please don't remove them! You'll be able to build without them, but you will experience crashes at runtime. We recognize that this is not an optimal setup. We will be hosting our SDK on a proper Maven repository set up in the future.
* We're assuming that you're using Android Studio for development. If you're not, please let us know! (But we'd prefer if you did)

## Using the SDK
1. The first step is to point your application at an instance of the Pison Server running on your local network. In this case, you'll want to use the Virtualized Pison Device included at the [root level of this repo](..) (usage instructions are there as well). Once you have that running on your development machine, copy the `address` and `port number`. 

2. From your Android application, create a new `PisonSdk` instance using:
```
// context can be Activity/Application instance
val sdk = newPisonSdkInstance(context).bindToServer(address: String, port: Int)
```
using the values you copied in step 1 for `address` and `port`. **Note that the address may change if your development machine's network connection changes!!**

3. Now you can monitor Pison Devices that connect to that server using:
```
val disposable = sdk.monitorAnyDevice()
    .subscribe(
        onNext = { pisonDevice ->
            // do cool stuff here
        }, 
        onError = { throwable ->
            println("error $throwable")
        })
```
You'll notice that our SDK exposes Reactive endpoints. Specifically, we're using the [Reaktive](https://github.com/badoo/Reaktive) library, which should look very familiar to anyone who has done Reactive programming before. If not, here is an [awesome introduction](https://blog.danlew.net/2017/07/27/an-introduction-to-functional-reactive-programming/).

You'll notice that we haven't provided documentation for the `pisonDevice` object in the example above. This is by design! We want to: 
1. See how far you can explore on your own! Everything at Pison is a moving target, and not everything is well documented. That's an unfortunate truth. We're always trying to get better, but frankly, we need developers who can thrive in these uncertain situations.
2. More importantly, we want to actively encourage a dialog. We know that writing software is an interactive experience. You wouldn't be working alone at Pison, so there's no reason for you to be working alone now. **Reach out to us with questions!** We don't have unlimited time, but we're happy to help where we can. 
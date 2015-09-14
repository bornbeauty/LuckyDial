# LuckyDial抽奖转盘
---
[项目地址](https://github.com/Jimbo-zjb/LuckyDial)

###简介
利用SurfaceView()来实现了转盘抽奖的功能
- 可以指定抽奖结果
- 可以添加回调方法在转动结束后来处理后续逻辑
![demo](http://c.picphotos.baidu.com/album/s%3D740%3Bq%3D90/sign=93bc378e848ba61edbeeca2b710fe637/a8ec8a13632762d0d58a95fba6ec08fa513dc639.jpg)
###公共方法介绍
- void stop()
- void stop(int stopIndex)
- void setHandler(Handler handler)
- boolean isRunning() 
- boolean isStoped()
- void start()
- int get()


1. stop()方法是用来结束抽奖了，为了更加方便我们的实际的应用，重载了stop()方法来指定抽奖结果，stopIndex就是物品数组的索引。
2. 通过setHandler()方法为抽奖盘停止转动后添加逻辑处理
3. isRunning()判断转盘是否还在转动，因为转盘在点击了停止按钮后还是会转动的，缓缓停下来。
4. isStoped()方法是判断是否已经点击了停止按钮
5. start()开启转盘
6. get()返回抽奖结果，如果此时并没有停止转动，将会得到-1

另外，已经排除了可能指在线上的问题






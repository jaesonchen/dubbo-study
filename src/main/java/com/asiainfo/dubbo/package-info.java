/**   
 *dubbo服务治理

    传输层 Transporter: mina, netty
    序列化 Serialization: dubbo, hessian2, jdk, json
    线程池 ThreadPool: fixed, cached, limit, eager
    
    配置覆盖
    配置覆盖原则：细粒度配置优先于粗粒度配置，consumer优先于provider配置
    
    启动时检查(check -> [consumer, reference])
    Dubbo cosumer缺省会在启动时检查依赖的服务是否可用，不可用时会抛出异常，阻止 Spring 初始化完成，以便上线时，能及早发现问题，默认 check="true"。
    可以通过 check="false" 关闭检查，比如，测试时，有些服务不关心，或者出现了循环依赖，必须有一方先启动。
    另外，如果你的 Spring 容器是懒加载的，或者通过 API 编程延迟引用服务，请关闭 check，否则服务临时不可用时，会抛出异常。
    关闭所有服务的启动时检查 (没有提供者时报错)：
    <dubbo:consumer check="false" />
    关闭单个服务的启动时检查 (没有提供者时报错)：
    <dubbo:reference interface="com.foo.BarService" check="false" />
    通过 dubbo.properties配置：
    dubbo.reference.com.foo.BarService.check=false
    dubbo.reference.check=false //强制改变所有 reference 的 check 值，就算配置中有声明，也会被覆盖。
    dubbo.consumer.check=false  //设置 check 的缺省值，如果配置中有显式的声明，如：<dubbo:reference check="true"/>，不会受影响。
    
    
    集群容错(cluster -> [service, reference], retries -> [service, method, reference, refer-menthod])
    在集群调用失败时，Dubbo 提供了多种容错方案，缺省为 failover 重试，通常配合retries使用。
  - Failover
    失败自动切换，当出现失败，重试其它服务器。通常用于读操作，但重试会带来更长延迟。可通过 retries="2" 来设置重试次数(不含第一次)。
    <dubbo:service retries="2" />
    <dubbo:reference retries="2" />
    <dubbo:reference>
        <dubbo:method name="findFoo" retries="2" />
    </dubbo:reference>
  - Failfast
    快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。
  - Failsafe
    失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。
  - Failback
    失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。
  - Forking
    并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。
  - Broadcast
    广播调用所有提供者，逐个调用，任意一台报错则报错 。通常用于通知所有提供者更新缓存或日志等本地资源信息。
    <dubbo:service cluster="failsafe" />
    <dubbo:reference cluster="failsafe" />
    
    
    负载均衡(loadbalance -> [service, method, reference, refer-menthod], parameter -> [hash.arguments, hash.nodes])
    Random LoadBalance
    随机，按权重设置随机概率（默认）。
    在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。
    RoundRobin LoadBalance
    轮询，按公约后的权重设置轮询比率。
    存在慢的提供者累积请求的问题。
    LeastActive LoadBalance
    最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
    使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。
    ConsistentHash LoadBalance
    一致性 Hash，相同参数的请求总是发到同一提供者。
    当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
    缺省只对方法第一个参数 Hash，如果要修改，请配置 <dubbo:parameter key="hash.arguments" value="0,1" />
    缺省用 160 份虚拟节点，如果要修改，请配置 <dubbo:parameter key="hash.nodes" value="320" />
    服务端
    <dubbo:service interface="..." loadbalance="roundrobin" />
    <dubbo:service interface="...">
        <dubbo:method name="..." loadbalance="roundrobin"/>
    </dubbo:service>
    客户端 <dubbo:reference interface="..." loadbalance="roundrobin" />
    <dubbo:reference interface="...">
        <dubbo:method name="..." loadbalance="roundrobin"/>
    </dubbo:reference>
    
    
    线程模型(dispatcher, threadpool, threads -> [protocol])
    dubbo线程模型类似于netty里的boss线程负责监听所有事件，没有work线程池的独立selector用于监听socket读事件，
    这里所谓的线程池就是用于处理一个socket里读写事件的Runnable Task的普通线程池。
    如果事件处理的逻辑能迅速完成，并且不会发起新的 IO 请求，比如只是在内存中记个标识，则直接在 IO 线程上处理更快，因为减少了线程池调度。
    但如果事件处理逻辑较慢，或者需要发起新的 IO 请求，比如需要查询数据库，则必须派发到线程池，否则 IO 线程阻塞，将导致不能接收其它请求。
    需要通过不同的派发策略和不同的线程池配置的组合来应对不同的场景:
    <dubbo:protocol name="dubbo" dispatcher="all" threadpool="fixed" threads="100" />
  - dispatcher
    all 所有消息都派发到线程池，包括请求，响应，连接事件，断开事件，心跳等。
    direct 所有消息都不派发到线程池，全部在 IO 线程上直接执行。
    message 只有请求响应消息派发到线程池，其它连接、断开事件，心跳等消息，直接在 IO 线程上执行。
  - threadpool
    fixed 固定大小线程池，启动时建立线程，不关闭，一直持有。(缺省)
    cached 缓存线程池，空闲一分钟自动删除，需要时重建。
    limited 可伸缩线程池，但池中的线程数只会增长不会收缩。只增长不收缩的目的是为了避免收缩时突然来了大流量引起的性能问题。
    eager 优先创建Worker线程池。在任务数量大于corePoolSize但是小于maximumPoolSize时，优先创建Worker来处理任务。
          当任务数量大于maximumPoolSize时，将任务放入阻塞队列中。阻塞队列充满时抛出RejectedExecutionException。
          (相比于cached:cached在任务数量超过maximumPoolSize时直接抛出异常而不是将任务放入阻塞队列)
  - threads 线程池大小
      
    
    协议(name -> [protocol, service, reference])
    Dubbo 允许配置多协议，在不同服务上支持不同协议或者同一服务上同时支持多种协议。
    不同服务在性能上适用不同协议进行传输，比如大数据用短连接协议，小数据大并发用长连接协议
    <!-- 多协议配置 -->
    <dubbo:protocol name="dubbo" port="20880" />
    <dubbo:protocol name="hessian" port="8080" />
    <!-- 使用dubbo协议暴露服务 -->
    <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" protocol="dubbo" />
    <!-- 使用hessian协议暴露服务 -->
    <dubbo:service interface="com.alibaba.hello.api.DemoService" version="1.0.0" ref="demoService" protocol="hessian" /> 
    <!-- 使用多个协议暴露服务，用逗号分隔 -->
    <dubbo:service interface="com.alibaba.hello.api.PublicService" version="1.0.0" protocol="dubbo,hessian" />
    
    
    注册中心(id, address, default -> [registry], registry -> [service, reference])
    Dubbo 支持同一服务向多注册中心同时注册，或者不同服务分别注册到不同的注册中心上去
    多注册中心注册
    <!-- 多注册中心配置，竖号分隔表示同时连接多个不同注册中心，同一注册中心的多个集群地址用逗号分隔 -->
    <dubbo:registry id="chinaRegistry" address="10.20.141.150:9090" />
    <!-- default 默认true -->
    <dubbo:registry id="intlRegistry" address="10.20.141.151:9010" default="false" />
    <!-- 向多个注册中心注册 -->
    <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" registry="hangzhouRegistry,qingdaoRegistry" />
    <!-- 不同服务使用不同注册中心 -->
    <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" registry="chinaRegistry" />
    <dubbo:service interface="com.alibaba.hello.api.DemoService" version="1.0.0" ref="demoService" registry="intlRegistry" />
    <!-- 多注册中心引用 -->
    <dubbo:reference id="chinaHelloService" interface="com.alibaba.hello.api.HelloService" version="1.0.0" registry="chinaRegistry" />
    <dubbo:reference id="intlHelloService" interface="com.alibaba.hello.api.HelloService" version="1.0.0" registry="intlRegistry" />
    
    
    服务分组(group -> [service, reference])
    当一个接口有多种实现时，可以用 group 区分。
    <dubbo:service group="feedback" interface="com.xxx.IndexService" />
    <dubbo:service group="member" interface="com.xxx.IndexService" />
    <dubbo:reference id="feedbackIndexService" group="feedback" interface="com.xxx.IndexService" />
    <dubbo:reference id="memberIndexService" group="member" interface="com.xxx.IndexService" />
    <!-- 任意组，2.2.0 以上版本支持，总是只调一个可用组的实现 -->
    <dubbo:reference id="barService" interface="com.foo.BarService" group="*" />
    
    
    版本(version -> [service, reference])
    当一个接口实现，出现不兼容升级时，可以用版本号过渡，版本号不同的服务相互间不引用。
    可以按照以下的步骤进行版本迁移：
    a. 在低压力时间段，先升级一半提供者为新版本
    b. 再将所有消费者升级为新版本
    c. 然后将剩下的一半提供者升级为新版本
    <dubbo:service interface="com.foo.BarService" version="1.0.0" />
    <dubbo:reference id="barService" interface="com.foo.BarService" version="1.0.0" />
    <!-- 不需要区分版本 -->
    <dubbo:reference id="barService" interface="com.foo.BarService" version="*" />
    
    
    结果缓存(cache -> [reference, refer-menthod])
    consumer端结果缓存，用于加速热门数据的访问速度，Dubbo 提供声明式缓存，以减少用户加缓存的工作量。
    lru 基于最近最少使用原则删除多余缓存，保持最热的数据被缓存。
    threadlocal 当前线程缓存，比如一个页面渲染，用到很多 portal，每个 portal 都要去查用户信息，通过线程缓存，可以减少这种多余访问。
    jcache 与 JSR107 集成，可以桥接各种缓存实现（如何桥接有待研究）。
    <dubbo:reference interface="com.foo.BarService" cache="lru" />
    <dubbo:reference interface="com.foo.BarService">
        <dubbo:method name="findBar" cache="lru" />
    </dubbo:reference>
    
    
    泛化调用(generic -> [reference])
    泛化接口调用方式主要用于客户端没有 API 接口及模型类型的情况，参数及返回值中的所有 POJO 均用 Map 表示，通常用于框架集成。
    GenericService 接口只有一个方法$invoke，服务端可以实现GenericService，也可以是普通的Service。
    <dubbo:reference id="barService" interface="com.foo.BarService" generic="true" />
    GenericService barService = (GenericService) applicationContext.getBean("barService");
    Object result = barService.$invoke("sayHello", new String[] { "java.lang.String" }, new Object[] { "World" });
    Map<String, Object> map = new HashMap<>();
    Object result = barService.$invoke("sayHello", new String[] { "com.asiainfo.User" }, new Object[] { map });
    
    // api编程
    // ReferenceConfig实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
    ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>(); 
    // 弱类型接口名
    reference.setInterface("com.xxx.XxxService");  
    reference.setVersion("1.0.0");
    // 声明为泛化接口 
    reference.setGeneric(true);  
    // 用org.apache.dubbo.rpc.service.GenericService可以替代所有接口引用  
    GenericService genericService = reference.get(); 
    // 基本类型以及Date, List, Map等不需要转换，直接调用
    Object result = genericService.$invoke("sayHello", new String[] {"java.lang.String"}, new Object[] {"world"}); 
    // 用Map表示POJO参数，如果返回值为POJO也将自动转成Map 
    Map<String, Object> map = new HashMap<>(); 
    map.put("name", "xxx"); 
    map.put("password", "yyy"); 
    // 如果返回POJO将自动转成Map 
    Object result = genericService.$invoke("findPerson", new String[] {"com.xxx.Person"}, new Object[]{ map }); 
    
    
    泛化实现
    泛化接口实现方式主要用于服务器端没有API接口及模型类型的情况，参数及返回值中的所有POJO均用Map表示，通常用于框架集成。
    比如：实现一个通用的远程服务Mock框架，可通过实现GenericService接口处理所有服务请求。
    <bean id="genericService" class="com.foo.MyGenericService" />
    <dubbo:service interface="com.foo.BarService" ref="genericService" />
    public class MyGenericService implements GenericService {
        public Object $invoke(String methodName, String[] parameterTypes, Object[] args) throws GenericException {
            if ("sayHello".equals(methodName)) {
                return "Welcome " + args[0];
            }
            throw new GenericException(getClass().getName(), "no such method(" + methodName + ").");
        }
    }
    
    // api 编程
    // 用org.apache.dubbo.rpc.service.GenericService可以替代所有接口实现
    GenericService xxxService = new XxxGenericService(); 
    // ServiceConfig实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
    ServiceConfig<GenericService> service = new ServiceConfig<GenericService>();
    // 弱类型接口名
    service.setInterface("com.xxx.XxxService");
    service.setVersion("1.0.0");
    // 指向一个通用服务实现
    service.setRef(xxxService);
    // 暴露及注册服务
    service.export();
    
    
    回声测试
    回声测试用于检测服务是否可用，回声测试按照正常请求流程执行，能够测试整个调用是否通畅，可用于监控。
    所有服务自动实现 EchoService 接口，只需将任意服务引用强制转型为 EchoService，即可使用。
    // 远程服务引用
    MemberService memberService = ctx.getBean("memberService");
    // 强制转型为EchoService
    EchoService echoService = (EchoService) memberService;
    // 回声测试可用性
    String status = echoService.$echo("OK"); 
    assert(status.equals("OK"));
    
    
    上下文信息RpcContext
    上下文中存放的是当前调用过程中所需的环境信息。
    RpcContext 是一个 ThreadLocal 的临时状态记录器，当接收到 RPC 请求，或发起 RPC 请求时，RpcContext 的状态都会变化。
    
    // rpc远程调用
    xxxService.xxx();
    // 本端是否为消费端，这里会返回true
    boolean isConsumerSide = RpcContext.getContext().isConsumerSide();
    // 获取最后一次调用的提供方IP地址
    String serverIP = RpcContext.getContext().getRemoteHost();
    // 获取当前服务配置信息，所有配置信息都将转换为URL的参数
    String application = RpcContext.getContext().getUrl().getParameter("application");
    // 注意：每次发起RPC调用，上下文状态会变化
    yyyService.yyy();
    
    // 服务提供方
    public class XxxServiceImpl implements XxxService {
        public void xxx() {
            // 本端是否为提供端，这里会返回true
            boolean isProviderSide = RpcContext.getContext().isProviderSide();
            // 获取调用方IP地址
            String clientIP = RpcContext.getContext().getRemoteHost();
            // 获取当前服务配置信息，所有配置信息都将转换为URL的参数
            String application = RpcContext.getContext().getUrl().getParameter("application");
            // 注意：每次发起RPC调用，上下文状态会变化
            yyyService.yyy();
            // 此时本端变成消费端，这里会返回false
            boolean isProviderSide = RpcContext.getContext().isProviderSide();
        } 
    }
    
    
    隐式参数
    可以通过 RpcContext 上的 setAttachment 和 getAttachment 在服务消费方和提供方之间进行参数的隐式传递。
    setAttachment 设置的 KV 对，在完成下面一次远程调用会被清空，即多次远程调用要多次设置。
    注意：path, group, version, dubbo, token, timeout 几个 key 是保留字段。
    // 消费端
    RpcContext.getContext().setAttachment("index", "1"); // 隐式传参，后面的远程调用会隐式将这些参数发送到服务器端，类似cookie，用于框架集成，不建议常规业务使用
    xxxService.xxx(); // 远程调用
    // 服务端
    public class XxxServiceImpl implements XxxService {
        public void xxx() {
            // 获取客户端隐式传入的参数，用于框架集成，不建议常规业务使用
            String index = RpcContext.getContext().getAttachment("index"); 
        }
    }
    
    
    异步调用(async -> [refer-menthod], return -> [refer-menthod])
    异步调用有两种：
    1. 服务端接口返回CompletableFuture，consumer是普通调用直接返回CompletableFuture。
    2. 服务端是普通服务，consumer通过设置async=true触发异步调用，通过RpcContext.getContext().getCompletableFuture()获得Future。
    
    从v2.7.0开始，Dubbo的所有异步编程接口开始以CompletableFuture为基础
    基于 NIO 的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小。
    异步处理逻辑（个人猜测）
    a. consumer端在异步调用时，会生成一个唯一id并封装request为AsyncRequest，socket写完AsyncRequest会立即返回一个CompletableFuture给调用者，
    然后在Threadlocal中缓存此id和CompletableFuture，服务端在处理完AsyncRequest后应该会返回一个AsyncResponse，里面包含id，
    socket在读到AsyncResponse后根据id从本地Threadlocal缓存中取出CompletableFuture，并把AsyncResponse的结果set进去，触发future的whenComplete和get。
    b. provider端在读到AsyncRequest后，封装成Task(AsyncRequest, socket, selector)任务直接丢到线程池中执行，在执行完成后，
    封装成AsyncResponse丢到待写队列里（这里不直接写socket个人是考虑会不会socket当时已经在写了），然后wakeup selector（感觉也可以直接interest(OP_WRITE)唤醒selector），
    boss主线程在处理完读事件后，从带写队列里取出所有的结果，并写到socket里。
    
    // 需要provider事先定义CompletableFuture签名的服务，也可以是provider定义一般的服务，然后consumer设置async=true
    public interface AsyncService {
        CompletableFuture<String> sayHello(String name);
    }
    return CompletableFuture.supplyAsync(() -> {
                System.out.println(RpcContext.getContext().getAttachment("consumer-key1"));
                return "async response from provider."; });
    // 消费端
    <dubbo:reference id="asyncService" timeout="10000" interface="com.alibaba.dubbo.samples.async.api.AsyncService"/>
    // 调用直接返回CompletableFuture
    CompletableFuture<String> future = asyncService.sayHello("async call request");
    // 增加回调
    future.whenComplete((retValue, exception) -> {
        if (exception != null) {
            exception.printStackTrace();
        } else {
            System.out.println("Response: " + retValue);
        }
    });
    System.out.println("Executed before response return.");
    
    // consumer定义async=true，使用RpcContext获得Future
    <dubbo:reference id="asyncService" interface="org.apache.dubbo.samples.governance.api.AsyncService">
          <dubbo:method name="sayHello" async="true" />
    </dubbo:reference>
    // 此调用会立即返回null
    asyncService.sayHello("world");
    // 拿到调用的Future引用，当结果返回后，会被通知和设置到此Future
    CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
    future.get();
    
    如果你只是想异步，完全忽略返回值，可以配置 return="false"，以减少 Future 对象的创建和管理成本：
    <dubbo:method name="findFoo" async="true" return="false" />
    
    
    
    参数回调(callback -> [argument])
    Dubbo 将基于长连接生成反向代理，这样就可以从服务器端调用客户端逻辑
    // provider端配置
    <dubbo:service interface="com.callback.CallbackService" ref="callbackService" connections="1">
        <dubbo:method name="addListener">
            <!--指定参数组下标确定callback-->
            <dubbo:argument index="1" callback="true" />
            <!--也可以通过指定类型的方式-->
            <!--<dubbo:argument type="com.demo.CallbackListener" callback="true" />-->
        </dubbo:method>
    </dubbo:service>
    // consumer端配置
    <dubbo:reference id="callbackService" interface="com.callback.CallbackService" />
    CallbackService callbackService = (CallbackService) context.getBean("callbackService");
    callbackService.addListener("foo.bar", new CallbackListener(){
        public void changed(String msg) {
            System.out.println("callback1:" + msg);
        }
    });
    
    
    事件通知(oninvoke, onreturn, onthrow -> [refer-menthod])
    在consumer 调用之前、调用之后、出现异常时，会触发 oninvoke、onreturn、onthrow 三个事件，可以配置当事件发生时，通知consumer 端哪个类的哪个方法 。
    // consumer 事件通知
    <bean id ="demoCallback" class = "org.apache.dubbo.callback.implicit.NofifyImpl" />
    <dubbo:reference id="demoService" interface="org.apache.dubbo.callback.implicit.IDemoService" version="1.0.0">
          <dubbo:method name="get" onreturn = "demoCallback.onreturn" onthrow="demoCallback.onthrow" />
    </dubbo:reference>
    
    
    本地伪装Mock (mock -> [reference])
    本地伪装通常用于服务降级，比如某验权服务，当服务提供方全部挂掉后，客户端不抛出异常，而是通过 Mock 数据返回授权失败。
    <dubbo:reference interface="com.foo.BarService" mock="true" />
    <dubbo:reference interface="com.foo.BarService" mock="com.foo.BarServiceMock" />
    <dubbo:reference id="demoService" check="false" interface="com.foo.BarService">
        <dubbo:parameter key="sayHello.mock" value="force:return fake"/>
    </dubbo:reference>
    public class BarServiceMock implements BarService {
        public String sayHello(String name) {
            // 你可以伪造容错数据，此方法只在出现RpcException时被执行
            return "容错数据";
        }
    }
    如果服务的consumer经常需要 try-catch 捕获异常，请考虑改为 Mock 实现，并在 Mock 实现中 return null。
    <dubbo:reference interface="com.foo.BarService" mock="return null" />
    
    mock中使用的关键字：
    a. return
    使用 return 来返回一个字符串表示的对象，作为 Mock 的返回值。合法的字符串可以是：
    empty: 代表空，基本类型的默认值，或者集合类的空值
    null: null
    true: true
    false: false
    JSON 格式: 反序列化 JSON 所得到的对象
    b. throw
    使用 throw 来返回一个 Exception 对象，作为 Mock 的返回值。
    当调用出错时，抛出一个默认的 RPCException:
    <dubbo:reference interface="com.foo.BarService" mock="throw" /> 
    当调用出错时，抛出指定的 Exception：
    <dubbo:reference interface="com.foo.BarService" mock="throw com.foo.MockException" />
    c. force 和 fail
    force: 代表强制使用 Mock 行为，在这种情况下不会走远程调用。
    fail:  与默认行为一致，只有当远程调用发生错误时才使用 Mock 行为。
    <dubbo:reference interface="com.foo.BarService" mock="force:return true" />
    
    
    延迟暴露(delay -> [service])
    如果你的服务需要预热时间，比如初始化缓存，等待相关资源就位等，可以使用 delay 进行延迟暴露。
    所有服务都将在 Spring 初始化完成后进行暴露，所以通常你不需要延迟暴露服务，无需配置 delay。
    <dubbo:service delay="5000" />
    
    
    并发控制(executes, actives -> [service, method, reference, refer-menthod])
    服务器端并发执行（或占用线程池线程数，这里应该是指的服务端的worker线程池里，同时在运行一个service/method的线程数，这些请求可能来自多个consumer）
    <dubbo:service interface="com.foo.BarService" executes="10" />
    <dubbo:service interface="com.foo.BarService">
        <dubbo:method name="sayHello" executes="10" />
    </dubbo:service>
    客户端并发执行（或占用连接的请求数，这里应该是指的单个socket长连接运行一个service/method的最大线程数，以免单个socket端请求量太大占用了太多线程资源）
    <dubbo:service interface="com.foo.BarService" actives="5" />
    <dubbo:reference interface="com.foo.BarService" actives="5" />
    
    
    连接控制(connections -> [service, reference], accepts -> [protocol])
    Dubbo 协议缺省每个service的consumer使用单一长连接（consumer端如果配有多个reference，则拥有多个长连接），如果数据量较大，可以使用多个连接。
    <dubbo:service connections="1"/>
    <dubbo:reference connections="1"/>
    为防止被大量连接撑挂，可在provider端限制接收连接数，以实现服务提供方自我保护。
    // 限制一个节点provider接受的来自consumer的长连接总数不能超过 100 个
    <dubbo:protocol name="dubbo" accepts="100" />
    // 限制客户端服务使用连接不能超过 10 个
    // 如果是长连接，比如 Dubbo 协议，connections 表示一个consumer节点可以对该服务单个节点provider建立的长连接数
    <dubbo:service interface="com.foo.BarService" connections="10" />
    <dubbo:reference interface="com.foo.BarService" connections="10" />
    
    
    粘滞连接(sticky -> [reference])
    粘滞连接用于有状态服务，尽可能让客户端总是向同一提供者发起调用，除非该提供者挂了，再连另一台。
    <dubbo:reference id="xxxService" interface="com.xxx.XxxService" sticky="true" />
    
    
    令牌验证(token -> [provider, protocol, service])
    通过令牌验证在注册中心控制权限，以决定要不要下发令牌给消费者，可以防止消费者绕过注册中心访问提供者，另外通过注册中心可灵活改变授权方式，而不需修改或升级提供者。
    <!--随机token令牌，使用UUID生成-->
    <dubbo:provider interface="com.foo.BarService" token="true" />
    <!--固定token令牌，相当于密码-->
    <dubbo:provider interface="com.foo.BarService" token="123456" />
    <!--固定token令牌，相当于密码-->
    <dubbo:protocol name="dubbo" token="123456" />
    <!--固定token令牌，相当于密码-->
    <dubbo:service interface="com.foo.BarService" token="123456" />
    
    
    服务降级
    可以通过服务降级功能临时屏蔽某个出错的非关键服务，并定义降级后的返回策略。
    向注册中心写入动态配置覆盖规则：
    RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
    Registry registry = registryFactory.getRegistry(URL.valueOf("zookeeper://10.20.153.10:2181"));
    registry.register(URL.valueOf("override://0.0.0.0/com.foo.BarService?category=configurators&dynamic=false&application=foo&mock=force:return+null"));
    mock=force:return+null 表示消费方对该服务的方法调用都直接返回 null 值，不发起远程调用。用来屏蔽不重要服务不可用时对调用方的影响。
    mock=fail:return+null  表示消费方对该服务的方法调用在失败后，再返回 null 值，不抛异常。用来容忍不重要服务不稳定时对调用方的影响。
    
    
    主机绑定
    缺省主机 IP 查找顺序：
    a. 通过 LocalHost.getLocalHost() 获取本机地址。
    b. 如果是 127.* 等 loopback 地址，则扫描各网卡，获取网卡IP。
    
    主机配置
    注册的地址如果获取不正确，可以：
    a. 可以在 /etc/hosts 中加入：机器名 公网IP
    master 205.182.23.201
    b. 在 provider.xml 配置文件中加入主机地址的配置：
    <dubbo:protocol host="205.182.23.201">
    c. 在 dubbo.properties 中加入主机地址的配置：
    dubbo.protocol.host=205.182.23.201
    
    端口配置(dubbo/20880、rmi/1099、hessian/80)
    a. 在 provider.xml 中加入主机地址的配置：
    <dubbo:protocol name="dubbo" port="20880">
    b. 在 dubbo.properties 中加入主机地址的配置：
    dubbo.protocol.dubbo.port=20880
    
    日志适配
    自 2.2.1 开始，dubbo 开始内置 log4j、slf4j、jcl、jdk 这些日志框架的适配，也可以通过以下方式显示配置日志输出策略：
    a. 在 dubbo.properties 中指定
    dubbo.application.logger=log4j
    b. 在 provider/consumer.xml 中配置
    <dubbo:application logger="log4j" />
    
    访问日志
    如果你想记录每一次请求信息，可开启访问日志，类似于apache的访问日志。
    a. 将访问日志输出到当前应用的log4j日志：
    <dubbo:protocol accesslog="true" />
    b. 将访问日志输出到指定文件：
    <dubbo:protocol accesslog="/data/dubbo/foo/bar.log" />
    
    
    netty4 (servier -> [provider, protocol], client -> [consumer])
    dubbo 2.5.6版本新增了对netty4通信模块的支持，启用方式如下
    provider端：
    <dubbo:provider server="netty4" />
    <dubbo:protocol server="netty4" />
    consumer端：
    <dubbo:consumer client="netty4" />
    
    
    序列化(serialization, optimizer -> [protocol])
    在Dubbo中使用高效的Java序列化（Kryo和FST）
    <dubbo:protocol name="dubbo" serialization="kryo"/>
    <dubbo:protocol name="dubbo" serialization="fst"/>
    要让Kryo和FST完全发挥出高性能，最好将那些需要被序列化的类注册到dubbo系统中
    // 注册被序列化类
    public class SerializationOptimizerImpl implements SerializationOptimizer {
        public Collection<Class> getSerializableClasses() {
            List<Class> classes = new LinkedList<Class>();
            classes.add(BidRequest.class);
            classes.add(BidResponse.class);
            return classes;
        }
    }
    <dubbo:protocol name="dubbo" serialization="kryo" optimizer="org.apache.dubbo.demo.SerializationOptimizerImpl"/>
    
    
    zookeeper 注册中心
    服务提供者启动时: 向 /dubbo/com.foo.BarService/providers 目录下写入自己的 URL 地址
    服务消费者启动时: 订阅 /dubbo/com.foo.BarService/providers 目录下的提供者 URL 地址，并向 /dubbo/com.foo.BarService/consumers 目录下写入自己的 URL 地址
    监控中心启动时:   订阅 /dubbo/com.foo.BarService 目录下的所有提供者和消费者 URL 地址。
    <dependency>
        <groupId>org.apache.zookeeper</groupId>
        <artifactId>zookeeper</artifactId>
        <version>3.4.13</version>
    </dependency>
    
    zookeeper支持以下功能：
    a. 当provider出现断电等异常停机时，注册中心能自动删除提供者信息
    b. 当注册中心重启时，能自动恢复注册数据，以及订阅请求
    c. 当设置 <dubbo:registry check="false" /> 时，记录失败注册和订阅请求，后台定时重试
    d. 可通过 <dubbo:registry username="admin" password="1234" /> 设置 zookeeper 登录信息
       可通过 <dubbo:registry group="dubbo" /> 设置 zookeeper 的根节点，不设置将使用无根树
       支持 * 号通配符 <dubbo:reference group="*" version="*" />，可订阅服务的所有分组和所有版本的提供者
    
    Dubbo 支持 zkclient 和 curator 两种 Zookeeper 客户端实现：
    从 2.2.0 版本开始缺省为 zkclient 实现，以提升 zookeeper 客户端的健状性。
    a. <dubbo:registry address="zookeeper://10.20.153.10:2181" client="zkclient" />
    b. <dubbo:registry address="zookeeper://10.20.153.10:2181?client=zkclient" />
    c. dubbo.registry.client=zkclient
    <dependency>
        <groupId>org.apache.curator</groupId>
        <artifactId>curator-framework</artifactId>
        <version>4.0.1</version>
        <exclusions>
            <exclusion>
                <groupId>io.netty</groupId>
                <artifactId>netty</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.zookeeper</groupId>
                <artifactId>zookeeper</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.apache.curator</groupId>
        <artifactId>curator-recipes</artifactId>
        <version>4.0.1</version>
        <exclusions>
            <exclusion>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-log4j12</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.24.Final</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.7</version>
    </dependency>
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo</artifactId>
        <version>2.7.0</version>
        <exclusions>
            <exclusion>
            <groupId>org.springframework</groupId>
            <artifactId>*</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>com.alibaba.boot</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
        <version>0.1.0</version>
    </dependency>
            
            
    Zookeeper 单机配置:
    <dubbo:registry address="zookeeper://10.20.153.10:2181" />
    <dubbo:registry protocol="zookeeper" address="10.20.153.10:2181" />
    Zookeeper 集群配置：
    <dubbo:registry address="zookeeper://10.20.153.10:2181?backup=10.20.153.11:2181,10.20.153.12:2181" />
    <dubbo:registry protocol="zookeeper" address="10.20.153.10:2181,10.20.153.11:2181,10.20.153.12:2181" />
    
    
    为什么不能传大包
    因 dubbo 协议采用单一长连接，如果每次请求的数据包大小为 500KByte，假设网络为千兆网卡，每条连接最大 7MByte，
    单个服务提供者的 TPS(每秒处理事务数)最大为：128MByte / 500KByte = 262。单个消费者调用单个服务提供者的 TPS(每秒处理事务数)最大为：7MByte / 500KByte = 14。
    如果能接受，可以考虑使用，否则网络将成为瓶颈。
    
    
    包名改造
    a. Maven坐标
    groupId 由 com.alibaba 改为 org.apache.dubbo
    b. package
    为了减少用户升级成本，让用户可以做到渐进式升级，2.7.0版本继续保留了一些常用基础API和SPIcom.alibaba.dubbo的支持。
    package 由 com.alibaba.dubbo 改为 org.apache.dubbo
    
    
    注解
    @Reference      消费端服务引用注解
    @Service        提供端服务暴露注解
    @EnableDubbo    激活dubbo注解
    
    编程API
    ReferenceConfig     Service配置采集和引用api编程接口
    ServiceConfig       Service配置采集和暴露api编程接口
    ApplicationConfig   Application配置采集API
    RegistryConfig      注册中心配置采集API
    ConsumerConfig      提供端默认配置采集API
    ProviderConfig      消费端默认配置采集API
    ProtocolConfig      RPC协议配置采集API
    ArcumentConfig      服务参数级配置采集API
    MethodConfig        服务方法级配置采集API
    ModuleConfig        服务治理Module配置采集API
    MonitorConfig       监控配置采集API
    RpcContext          编程上下文API
    
    SPI扩展
    Registry        包括RegistryFactory, Registry ,RegistryService等扩展点
    Protocol        RPC协议扩展
    Serialization   序列化协议扩展
    Cluster         集群容错策略扩展，如Failover, Failfast等
    Loadbalance     负载均衡策略扩展
    Transporter     传输框架扩展，如Netty等
    Monitor         监控中心扩展，包括MonitorFactory, Monitor, MonitorService等
    Router          路由规则扩展
    Filter          拦截器扩展
    
    
    最佳实践
    a. 分包
    建议将服务接口、服务模型、服务异常等均放在 API 包中，因为服务模型和异常也是 API 的一部分，这样做也符合分包原则：重用发布等价原则(REP)，共同重用原则(CRP)。
    如果需要，也可以考虑在 API 包中放置一份 Spring 的引用配置，这样使用方只需在 Spring 加载过程中引用此配置即可。配置建议放在模块的包目录下，以免冲突。
    b. 粒度
    服务接口尽可能大粒度，每个服务方法应代表一个功能，而不是某功能的一个步骤，否则将面临分布式事务问题，Dubbo 暂未提供分布式事务支持。
    服务接口建议以业务场景为单位划分，并对相近业务做抽象，防止接口数量爆炸。
    不建议使用过于抽象的通用接口，这样的接口没有明确语义，会给后期维护带来不便。
    c. 版本
    每个接口都应定义版本号，为后续不兼容升级提供可能。
    建议使用两位版本号，因为第三位版本号通常表示兼容升级，只有不兼容时才需要变更服务版本。
    当不兼容时，先升级一半提供者为新版本，再将消费者全部升为新版本，然后将剩下的一半提供者升为新版本。
    d. 兼容性
    服务接口增加方法，或服务模型增加字段，可向后兼容，删除方法或删除字段，将不兼容，枚举类型新增字段也不兼容，需通过变更版本号升级。
    e. 枚举值
    如果是完备集，可以用 Enum，比如：ENABLE, DISABLE。
    如果是业务种类，以后明显会有类型增加，不建议用 Enum，可以用 String 代替。
    如果是在返回值中用了 Enum，并新增了 Enum 值，建议先升级服务消费方，这样服务提供方不会返回新值。
    如果是在传入参数中用了 Enum，并新增了 Enum 值，建议先升级服务提供方，这样服务消费方不会传入新值。
    f. 序列化
    服务参数及返回值建议使用 POJO 对象，即通过 setter, getter 方法表示属性的对象。
    服务参数及返回值不建议使用接口，因为数据模型抽象的意义不大，并且序列化需要接口实现类的元信息，并不能起到隐藏实现的意图。
    服务参数及返回值都必须是传值调用，而不能是传引用调用，消费方和提供方的参数或返回值引用并不是同一个，只是值相同，Dubbo 不支持引用远程对象。
    g. 异常
    建议使用异常汇报错误，而不是返回错误码，异常信息能携带更多信息，并且语义更友好。
    如果担心性能问题，在必要时，可以通过 override 掉异常类的 fillInStackTrace() 方法为空方法，使其不拷贝栈信息。
    查询方法不建议抛出 checked 异常，否则调用方在查询时将过多的 try...catch，并且不能进行有效处理。
    服务提供方不应将 DAO 或 SQL 等异常抛给消费方，应在服务实现中对消费方不关心的异常进行包装，否则可能出现消费方无法反序列化相应异常。
    h. 调用
    不要只是因为是 Dubbo 调用，而把调用 try...catch 起来。try...catch 应该加上合适的回滚边界上。
    Provider 端需要对输入参数进行校验。如有性能上的考虑，服务实现者可以考虑在 API 包上加上服务 Stub 类来完成检验。
    
    
    推荐用法
    a. 在 Provider 端配置合理的 Provider 端属性
    <dubbo:protocol threads="200" /> 
    <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService"
        executes="200" >
        <dubbo:method name="findAllPerson" executes="50" />
    </dubbo:service>
    
    建议在 Provider 端配置的 Provider 端属性有：
    threads：服务线程池大小
    executes：一个服务提供者并行执行请求上限，即当 Provider 对一个服务的并发调用达到上限后，新调用会阻塞，此时 Consumer 可能会超时。
              在方法上配置 dubbo:method 则针对该方法进行并发限制，在接口上配置 dubbo:service，则针对该服务进行并发限制
    
    b. 在 Provider 端尽量多配置 Consumer 端属性
      作服务的提供方，比服务消费方更清楚服务的性能参数，如调用的超时时间、合理的重试次数等。
      在 Provider 端配置后，Consumer 端不配置则会使用 Provider 端的配置，即 Provider 端的配置可以作为 Consumer 的缺省值 。
      否则，Consumer 会使用 Consumer 端的全局设置，这对于 Provider 是不可控的，并且往往是不合理的
    <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService"
        timeout="300" retry="2" loadbalance="random" actives="0" />
    <dubbo:service interface="com.alibaba.hello.api.WorldService" version="1.0.0" ref="helloService"
        timeout="300" retry="2" loadbalance="random" actives="0" >
        <dubbo:method name="findAllPerson" timeout="10000" retries="9" loadbalance="leastactive" actives="5" />
    <dubbo:service/>
    
    建议在 Provider 端配置的 Consumer 端属性有：
    timeout：方法调用的超时时间
    retries：失败重试次数，缺省是 2
    loadbalance：负载均衡算法，缺省是随机 random。还可以配置轮询 roundrobin、最不活跃优先 leastactive 和一致性哈希 consistenthash 等
    actives：消费者端的最大并发调用限制，即当 Consumer 对一个服务的并发调用到上限后，新调用会阻塞直到超时，
             在方法上配置 dubbo:method 则针对该方法进行并发限制，在接口上配置 dubbo:service，则针对该服务进行并发限制
    
    c. 配置管理信息
    目前有负责人信息和组织信息用于区分站点。以便于在发现问题时找到服务对应负责人，建议至少配置两个人以便备份。负责人和组织信息可以在运维平台 (Dubbo Ops) 上看到。
    在应用层面配置负责人、组织信息：
    <dubbo:application owner=”ding.lid,william.liangf” organization=”intl” />
    在服务层面（服务端）配置负责人：
    <dubbo:service owner=”ding.lid,william.liangf” />
    在服务层面（消费端）配置负责人：
    <dubbo:reference owner=”ding.lid,william.liangf” />
    
    d. 配置 Dubbo 缓存文件
    该文件会缓存注册中心列表和服务提供者列表。配置缓存文件后，应用重启过程中，若注册中心不可用，应用会从该缓存文件读取服务提供者列表，进一步保证应用可靠性。
    注意：如果有多个应用进程，请注意不要使用同一个文件，避免内容被覆盖。
    提供者列表缓存文件：
    <dubbo:registry file=”${user.home}/output/dubbo.cache” />
    
    e. 不要使用 dubbo.properties 文件配置，推荐使用对应 XML 配置
    Dubbo 中所有的配置项都可以配置在 Spring 配置文件中，并且可以针对单个服务配置。如完全不配置则使用 Dubbo 缺省值。

 * 
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午9:53:05
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
package com.asiainfo.dubbo;


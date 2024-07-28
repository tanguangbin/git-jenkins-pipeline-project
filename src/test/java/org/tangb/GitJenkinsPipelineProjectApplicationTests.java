package org.tangb;


import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class GitJenkinsPipelineProjectApplicationTests {
//    @Autowired
//    private  UserMapper userMapper;
//    @Autowired
//    private IUserService userService;

    @Test
    void contextLoads() {
    }


//    @Test
//    void getUserList(){
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        wrapper.select("id","userName");
//        wrapper.like("userName","admin");
//        List<User> users = userMapper.selectList(wrapper);
//        System.out.println(users);
//    }
//
//    @Test
//    void getUserListByLamda(){
//        LambdaQueryWrapper <User> lambdaWrapper = new LambdaQueryWrapper<>();
//        lambdaWrapper.select(User::getNickname,User::getUsername);
//        lambdaWrapper.like(User::getUsername,"admin");
//        List<User> users = userMapper.selectList(lambdaWrapper);
//        System.out.println(users);
//    }
//
//    @Test
//    void updateNickname(){
//
//
////        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
////        updateWrapper.setSql("nickname = CONCAT(nickname, '123')");
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(User::getId,"1");
//        int update = userMapper.updateNicknameById(lambdaQueryWrapper,"456");
//    }
//
//
//    private static final int BATCH_SIZE = 2000; // 每批次插入的记录数
//    private static final int THREAD_COUNT = 25; // 使用的线程数量
//
//    @Test
//    public void batchInsertTestWithThread() {
//        // 模拟10万条记录的数量
//        int totalRecords = 1000000;
//
//        int batchCount = (totalRecords + BATCH_SIZE - 1) / BATCH_SIZE; // 计算需要的批次数量
//
//        // 创建固定数量的线程池
//        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//        for (int i = 0; i < batchCount; i++) {
//            final int start = i * BATCH_SIZE;
//            final int end = Math.min((i + 1) * BATCH_SIZE, totalRecords);
//            final int batchIndex = i;
//            // 创建一个List来保存当前批次的数据
//            List<User> batch = new ArrayList<>();
//            for (int j = start; j < end; j++) {
//                User user = new User();
//
//                    user.setUsername("User" + j);
//
//                user.setAge((int) (Math.random() * 100));
//                // 其他字段的初始化
//                batch.add(user);
//            }
//
//            // 提交批量插入任务给线程池
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                try {
//                    insertBatchWithTransaction(batch,batchIndex); // 使用 saveBatch 方法插入数据
//                } catch (Exception e) {
//                    e.printStackTrace(); // 处理异常
//                }
//            }, executorService);
//            futures.add(future);
//        }
//
//        // 等待所有线程完成
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        // 关闭线程池
//        executorService.shutdown();
//    }
//
//
//    public void insertBatchWithTransaction(List<User> batch, int batchIndex) throws Exception {
//        // 模拟错误：当批次索引为5时抛出异常
////        if (batchIndex == 5) {
////            throw new Exception("模拟错误，批次 " + batchIndex + " 插入失败");
////        }
//
//        userService.saveOrUpdateBatch(batch, BATCH_SIZE); // 使用 saveBatch 方法插入数据
//    }
//
//    @Test
//    public void batchInsertTest() {
//        try {
//            batchInsertWithTransaction(); // 将所有操作放在一个事务方法中
//        } catch (Exception e) {
//            e.printStackTrace(); // 处理异常
//        }
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void batchInsertWithTransaction() throws Exception {
//        int totalRecords = 1000000; // 模拟100万条记录
//        int batchCount = (totalRecords + BATCH_SIZE - 1) / BATCH_SIZE; // 计算需要的批次数量
//
//        for (int i = 0; i < batchCount; i++) {
//            final int start = i * BATCH_SIZE;
//            final int end = Math.min((i + 1) * BATCH_SIZE, totalRecords);
//            final int batchIndex = i;
//
//            // 创建一个List来保存当前批次的数据
//            List<User> batch = new ArrayList<>(BATCH_SIZE);
//            for (int j = start; j < end; j++) {
//                User user = new User();
//                user.setUsername("User" + j);
//                user.setAge((int) (Math.random() * 100));
//                batch.add(user);
//            }
//
//            // 插入数据，模拟错误
//            insertBatchWithTransaction(batch, batchIndex);
//        }
//    }
}

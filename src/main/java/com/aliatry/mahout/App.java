package com.aliatry.mahout;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 * @author Simon
 */
public class App {
    public static void main(String[] args) throws Exception {

        // 初始化
        initial();

        // 基于用户的协同过滤算法
        baseUser();
        // 基于物品的协同过滤算法
        baseItem();


        // TODO 基于用户的协同过滤算法
        // 从文件加载数据
        DataModel model = new FileDataModel(new File("D:\\data.csv"));
        // 指定用户相似度计算方法，这里采用皮尔森相关度
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        // 指定用户邻居数量，这里为2
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);
        // 构建基于用户的推荐系统
        Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        // 得到指定用户的推荐结果，这里是得到用户1的两个推荐
        List<RecommendedItem> recommendations = recommender.recommend(1, 2);
        // 打印推荐结果
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
    }

    private static void initial() {
        FileOutputStream out = null;

        try {
            int[][] arg = {
                    {3, 1, 4, 4, 1, 0, 0},
                    {0, 5, 1, 0, 0, 4, 0},
                    {1, 0, 5, 4, 3, 5, 2},
                    {3, 1, 4, 3, 5, 0, 0},
                    {5, 2, 0, 1, 0, 5, 5}
            };
            out = new FileOutputStream(new File("D:\\data.csv"));
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 7; j++) {
                    if (arg[i][j] > 0) {
                        String a = i + "," + j + "," + arg[i][j] + "\n";
                        out.write(a.getBytes());
                    }
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 基于用户的协同过滤算法
     *
     * @throws IOException
     * @throws TasteException
     */
    private static void baseUser() throws IOException, TasteException {

        // 用户邻居数量
        final int NEIGHBORHOOD_NUM = 2;
        // 推荐结果个数
        final int RECOMMENDER_NUM = 3;

        /*
        准备数据 这里是电影评分数据
        数据集，其中第一列表示用户id；第二列表示商品id；第三列表示评分，评分是5分制
        */
        /*
        将数据加载到内存中
        基于文件的model，通过文件形式来读入,且此类型所需要读入的数据的格式要求很低，只需要满足每一行是用户id，物品id，用户偏好，且之间用tab或者是逗号隔开即可
        */
        DataModel dataModel = new FileDataModel(new File("D:\\data.csv"));
        /*
        计算相似度，相似度算法有很多种，欧几里得、皮尔逊等等。
        基于用户的协同过滤算法，基于物品的协同过滤算法，这里使用了EuclideanDistanceSimilarity
        计算欧式距离，欧式距离来定义相似性，用s=1/(1+d)来表示，范围在[0,1]之间，值越大，表明d越小，距离越近，则表示相似性越大
        */
        UserSimilarity similarity = new EuclideanDistanceSimilarity(dataModel);
        /*
        计算最近邻域，邻居有两种算法，基于固定数量的邻居和基于相似度的邻居，这里使用基于固定数量的邻居。
        NEIGHBORHOOD_NUM指定用户邻居数量
        */
        NearestNUserNeighborhood neighbor = new NearestNUserNeighborhood(NEIGHBORHOOD_NUM, similarity, dataModel);
        /*
        构建推荐器，协同过滤推荐有两种，分别是基于用户的和基于物品的，这里使用基于用户的协同过滤推荐
        构建基于用户的推荐系统
        */
        Recommender r = new GenericUserBasedRecommender(dataModel, neighbor, similarity);

        // 得到所有用户的id集合
        LongPrimitiveIterator iter = dataModel.getUserIDs();
        while (iter.hasNext()) {
            long uid = iter.nextLong();
            // 获取推荐结果，获取指定用户指定数量的推荐结果
            List<RecommendedItem> list = r.recommend(uid, RECOMMENDER_NUM);
            System.out.printf("user : %s", uid);
            // 遍历推荐结果
            for (RecommendedItem item : list) {
                // 获取推荐结果和推荐度
                System.out.print(item.getItemID() + "[" + item.getValue() + "] ");
            }
            System.out.println();
        }
    }

    /**
     * 基于物品的协同过滤算法
     *
     * @throws IOException
     * @throws TasteException
     */
    private static void baseItem() throws IOException, TasteException {
        DataModel dataModel = new FileDataModel(new File("D:\\data.csv"));
        /*
        计算相似度，相似度算法有很多种，欧几里得、皮尔逊等等。
        这里使用的是皮尔逊PearsonCorrelationSimilarity
        */
        ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
        //构建推荐器，协同过滤推荐有两种，分别是基于用户的和基于物品的，这里使用基于物品的协同过滤推荐
        GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, itemSimilarity);
        // 给指定用户推荐若干个与指定商品相似的商品
        List<RecommendedItem> recommendedItemList = recommender.recommendedBecause(1, 5, 2);
        // 打印推荐的结果
        System.out.println("根据用户1当前浏览的商品5，推荐2个相似的商品");
        for (RecommendedItem recommendedItem : recommendedItemList) {
            System.out.println(recommendedItem);
        }

    }
}
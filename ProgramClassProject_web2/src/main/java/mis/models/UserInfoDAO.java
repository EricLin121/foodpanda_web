package mis.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserInfoDAO
{
    private Connection conn;

    public UserInfo getUserInfoFromID(int id)
    {
        conn = DBConnection.getConnection();
        UserInfo userInfo = new UserInfo();
        String query = "SELECT `username`,`address`,`phone` FROM `user_info` WHERE `user_id` = ? LIMIT 1";
        //String query = "SELECT Max(order_num) as `max_id` FROM `sale_order` LIMIT 1";
        try
        {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, String.valueOf(id));
            ResultSet rset = state.executeQuery();
            if (rset.isBeforeFirst())
            {
                while (rset.next())
                {
                    userInfo.setName(rset.getString("username"));
                    userInfo.setAddress(rset.getString("address"));
                    userInfo.setPhone(rset.getString("phone"));
                }
            }
            else
            {
                userInfo = null;
            }

        }
        catch (SQLException ex)
        {
            System.out.println("資料庫確認密碼操作異常:" + ex.toString());
        }
        return userInfo;
    }

    public Map<Boolean, String> insertUserInfo(UserInfo user)
    {
        //String order_num =  getMaxOrderNum();
        conn = DBConnection.getConnection();
        Map<Boolean, String> confirm = new HashMap<>();
        String select = "SELECT * FROM `user_info` WHERE `user_id` = ? LIMIT 1";
        ResultSet rset = null;
        //String query = "INSERT INTO `order_detail` (`order_num`, `product_id`, `quantity`) VALUES (?, ?, ?)";
        try
        {
            PreparedStatement state = conn.prepareStatement(select);
            state.setString(1, String.valueOf(user.getUser_id()));
            rset = state.executeQuery();
            if (rset.isBeforeFirst())
            {
                update(user);
                confirm.put(true, "更新會員資料成功");
            }
            else
            {
                insert(user);
                confirm.put(true, "新增會員資料成功");
            }
        }
        catch (SQLException ex)
        {
            System.out.println("select異常:" + ex.toString());
        }

        return confirm;
    }

    public void update(UserInfo user)
    {
        conn = DBConnection.getConnection();
        String query = "update user_info set username=?, address=?, phone= ? where user_id = ?";
        try
        {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(4, user.getUser_id());
            state.setString(1, user.getName());
            state.setString(2, user.getAddress());
            state.setString(3, user.getPhone());

            state.executeUpdate();
        }
        catch (SQLException ex)
        {
            System.out.println("update異常:" + ex.toString());
        }
    }

    public void insert(UserInfo user)
    {
        conn = DBConnection.getConnection();
        String query = "INSERT INTO `user_info` (`user_id`,`username`, `address`,`phone`) VALUES (?,?, ?, ?)";
        try
        {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, user.getUser_id());
            state.setString(2, user.getName());
            state.setString(3, user.getAddress());
            state.setString(4, user.getPhone());

            state.executeUpdate();
        }
        catch (SQLException ex)
        {
            System.out.println("insert異常:" + ex.toString());
        }
    }
}

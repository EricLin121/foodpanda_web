package mis.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDAO
{

    private Connection conn;

    public Map<Boolean, String> confirm(User user)
    {
        conn = DBConnection.getConnection();
        Map<Boolean, String> confirm = new HashMap<>();
        String password = null;
        String query = "SELECT `password` FROM `user` WHERE `email` = ? LIMIT 1";
        //String query = "SELECT Max(order_num) as `max_id` FROM `sale_order` LIMIT 1";
        try
        {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, user.getEmail());
            ResultSet rset = state.executeQuery();
            if (rset.isBeforeFirst())
            {
                while (rset.next())
                {
                    password = rset.getString("password");
                    if (password.equals(user.getPassword()))
                    {
                        confirm.put(true, "登入成功");
                    }
                    else
                    {
                        confirm.put(false, "密碼錯誤");
                    }
                }
            }
            else
            {
                confirm.put(false, "帳號未註冊");
            }
        }
        catch (SQLException ex)
        {
            System.out.println("資料庫確認密碼操作異常:" + ex.toString());
        }
        return confirm;
    }

    public String getPermissionFromUser(User user)
    {
        //String order_num =  getMaxOrderNum();
        conn = DBConnection.getConnection();
        String select = "SELECT `permission` FROM `user` WHERE `email` = ? LIMIT 1";
        String permission = "";
        ResultSet rset = null;
        //String query = "INSERT INTO `order_detail` (`order_num`, `product_id`, `quantity`) VALUES (?, ?, ?)";
        try
        {
            PreparedStatement state = conn.prepareStatement(select);
            state.setString(1, user.getEmail());
            rset = state.executeQuery();
            while(rset.next())
            {
                permission = rset.getString("permission");
            }

        }
        catch (SQLException ex)
        {
            System.out.println("取得permission異常:" + ex.toString());
        }

        return permission;
    }

    public int getIdFromUser(User user)
    {
        //String order_num =  getMaxOrderNum();
        conn = DBConnection.getConnection();
        String select = "SELECT `user_id` FROM `user` WHERE `email` = ? LIMIT 1";
        int id = 0;
        ResultSet rset = null;
        //String query = "INSERT INTO `order_detail` (`order_num`, `product_id`, `quantity`) VALUES (?, ?, ?)";
        try
        {
            PreparedStatement state = conn.prepareStatement(select);
            state.setString(1, user.getEmail());
            rset = state.executeQuery();
            while(rset.next())
            {
                id =Integer.parseInt(rset.getString("user_id"));
            }

        }
        catch (SQLException ex)
        {
            System.out.println("取得id異常:" + ex.toString());
        }

        return id;
    }

    public Map<Boolean, String> insertUserAccount(User user)
    {
        //String order_num =  getMaxOrderNum();
        conn = DBConnection.getConnection();
        Map<Boolean, String> confirm = new HashMap<>();
        String select = "SELECT `password` FROM `user` WHERE `email` = ? LIMIT 1";
        String query = "INSERT INTO `user` (`email`, `password`) VALUES (?, ?)";
        ResultSet rset = null;
        //String query = "INSERT INTO `order_detail` (`order_num`, `product_id`, `quantity`) VALUES (?, ?, ?)";
        try
        {
            PreparedStatement state = conn.prepareStatement(select);
            state.setString(1, user.getEmail());
            rset = state.executeQuery();
            if (rset.isBeforeFirst())
            {
                confirm.put(false, "帳號已被註冊，請使用其他帳號");
            }
            else
            {
                state = conn.prepareStatement(query);
                state.setString(1, user.getEmail());
                state.setString(2, user.getPassword());
                state.execute();
                confirm.put(true, "註冊成功");
            }
        }
        catch (SQLException ex)
        {
            System.out.println("select異常:" + ex.toString());
        }

        return confirm;
    }

    public static void main(String[] args)
    {

        OrderDAO orddao = new OrderDAO();
        System.out.println(orddao.getMaxOrderNum());

        String ordNum = "ord102";

        Order cart = new Order(ordNum, "2021-05-01", 123, "李大同");
        orddao.insertCart(cart);

        OrderDetail crti = new OrderDetail();
        crti.setOrder_num(ordNum);
        crti.setQuantity(2);
        crti.setProduct_id("p-j-103");

        orddao.insertCartItem(crti);
    }

}

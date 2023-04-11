/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ask_Name_Address;

import Second_buy.SecondController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import models.Order;
import models.OrderDAO;
import models.OrderDetail;

/**
 * FXML Controller class
 *
 * @author user
 */
public class AskScreenController implements Initializable {
    models.OrderDAO orderDao = new OrderDAO();
    private SecondController second = SecondController.getInstance();

    @FXML
    private Label PriceOnButton;
    @FXML
    private TextArea display2;
    @FXML
    private TextField cus_name;
    @FXML
    private TextField cus_Address;
    @FXML
    private TextField cus_Phone;
    @FXML
    private CheckBox contactless;
    @FXML
    private CheckBox cutlery;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    //結帳*******************這裡寫入訂單明細到資料庫
    @FXML
    private void check2(ActionEvent event) {
        display2.setText("已結帳，發票列印中...\n");
        
         //append_order_to_csv(); //將這一筆訂單附加儲存到檔案或資料庫
        //這裡要取得不重複的order_num編號
        String order_num = orderDao.getMaxOrderNum();

        if (order_num == null) {
            order_num = "ord-100";
        }

        System.out.println(order_num);
        System.out.println(order_num.split("-")[1]);

        //將現有訂單編號加上1
        int serial_num = Integer.parseInt(order_num.split("-")[1]) + 1;
        System.out.println(serial_num);

        //每家公司都有其訂單或產品的編號系統，這裡用ord-xxx表之
        String new_order_num = "ord-" + serial_num;
        
        Boolean Contactless=contactless.isSelected();
        System.out.println("免洗餐具:"+Contactless);
        Boolean Cutlery=cutlery.isSelected();
        System.out.println("防疫取餐:"+Cutlery);
        
        
        

        int sum = second.check_total();
        //Cart crt = new Cart(new_order_num, "2021-05-01", 123, userName);
        Order crt = new Order();
        crt.setOrder_num(new_order_num);
        crt.setTotal_price(sum);
        crt.setCustomer_name(cus_name.getText());
        crt.setCustomer_phone(cus_Phone.getText());
        crt.setCustomer_address(cus_Address.getText());
        crt.setContactless(Contactless ? 1 : 0);
        crt.setCutlery(Cutlery ? 1 : 0);
        System.out.println(cus_name.getText());
        System.out.println(cus_Phone.getText());
        System.out.println(cus_Address.getText());

        //寫入一筆訂單道資料庫
        orderDao.insertCart(crt);

        //逐筆寫入訂單明細
        for (int i = 0; i < second.order_list.size(); i++) {
            OrderDetail item = new OrderDetail();
            item.setOrder_num(new_order_num); //設定訂單編號
            item.setProduct_id(second.order_list.get(i).getProduct_id()); //設定產品編號
            item.setQuantity(second.order_list.get(i).getQuantity().getSelectionModel().getSelectedIndex()+1);//設定訂購數量 多少杯/////////////////////////////////////////
            item.setProduct_price(second.order_list.get(i).getProduct_price()); //產品單價 建議不要這個欄位 不符合正規化
            item.setProduct_name(second.order_list.get(i).getProduct_name());//產品名稱 建議不要這個欄位 不符合正規化

            orderDao.insertOrderDetailItem(item);
        }

        second.order_list.clear();
        AskFXMain.closeWindows();
        
        
    }


    
}

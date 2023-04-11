package Second_buy;

import Ask_Name_Address.AskFXMain;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import models.Order;
import models.OrderDAO;
import models.OrderDetail;
import models.Product;
import models.ProductDAO;

public class SecondController implements Initializable {

    private static SecondController Second;
    //放置飲料選單的窗格/視窗
    @FXML
    private AnchorPane menuPane;

    //放所有產品  產品編號  產品物件
    private final TreeMap<String, Product> product_dict = new TreeMap();

    //置放訂單明細項目，也是給表格顯示的資料項目
    public static ObservableList<OrderDetail> order_list;

    //點選的產品，當把產品放入購物籃會用到
    private Product selectedItem;

    //private ComboBoxTableCell TableViewComboBox;
    //顯示點選到的產品名稱價格圖片
    @FXML
    private Label item_name;
    @FXML
    private Label item_price;
    @FXML
    private ImageView item_image;

    //買幾杯   
    //顯示訂單內容的表格 裡面存放的是OrderDetail訂單細節項目
    @FXML
    private TableView<OrderDetail> table;

    //顯示訂單總金額
    @FXML
    private TextArea display;

    // 存取上一個被點選的物件
    private OrderDetail tf;
    //存取上一個備存取的行數
    private int copy_row = -1;
    private int copy_row_forRightSide = -1;

    //***********產生資料DAO來使用
    ProductDAO productDao = new ProductDAO();
    OrderDAO orderDao = new OrderDAO();
    @FXML
    private Label item_describtion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Second = this;
        //讀產品 從資料庫中讀入
        prepareProduct();

        //將所有可視元件初始化布置好，事件也準備好
        initMyComponents();
    }

//準備產品的字典 從資料庫中讀入
    private void prepareProduct() {

        //***************從檔案或資料庫讀入產品菜單資訊
        List<Product> products = productDao.getAllProducts();

        //放入product_dict中點選產品與顯示產品比較方便
        for (Product product : products) {
            System.out.println(product.getCategory());
            product_dict.put(product.getProduct_id(), product);
        }

    }

//初始化元件
    public void initMyComponents() {

        selectedItem = product_dict.firstEntry().getValue();

        //ObservableList    order_list有新增或刪除都會處動table的更新，也就是發生任何改變時都被通知
        order_list = FXCollections.observableArrayList();

        //表格table塞入三個欄位
        TableColumn<OrderDetail, String> order_item_name = new TableColumn("品名");
        order_item_name.setCellValueFactory(new PropertyValueFactory("product_name"));

        TableColumn<OrderDetail, Integer> order_item_price = new TableColumn<>("價格");
        order_item_price.setCellValueFactory(new PropertyValueFactory<>("product_price"));

        TableColumn<OrderDetail, String> order_item_qty = new TableColumn("數量");
        order_item_qty.setCellValueFactory(new PropertyValueFactory<OrderDetail, String>("quantity"));
        //order_item_qty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        //order_item_qty.setCellFactory(TableViewComboBox.forTableColumn("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));

        table.setEditable(true);

        table.getColumns().addAll(order_item_name, order_item_price, order_item_qty);

        order_item_qty.setOnEditStart(new EventHandler<TableColumn.CellEditEvent<OrderDetail, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                System.out.println("copy_row ：" + copy_row);
                //印出細節內容看看裡面的內容是甚麼  //System.out.println(event.getTablePosition());  //System.out.println(event.getTablePosition().getRow());  //System.out.println(((Order)event.getTableView().getItems().get(event.getTablePosition().getRow())).getQty());  //System.out.println(event.getTableView().getItems().get(event.getTablePosition().getRow()));  //int new_val = Integer.parseInt((String)event.getNewValue());
                int row_num = event.getTablePosition().getRow();
                System.out.println("row_num ：" + row_num);

                if (row_num != copy_row) {
                    if (copy_row != -1) {
                        OrderDetail target = (OrderDetail) table.getItems().get(copy_row);   //
                        target.getQuantity().setDisable(true);
                    } 
                }
                OrderDetail target = (OrderDetail) event.getTableView().getItems().get(row_num);   //取得選定那一行的物件
                target.getQuantity().setDisable(false);
                copy_row = row_num;

                //
                //System.out.println(new_val);  //  target.setQuantity(new_val);  //System.out.println(order_list.get(row_num).getQuantity());  // check_total();
                //顯示總金額於display
                //String totalmsg = String.format("%s %d\n", "總金額:", check_total());
                // display.setText(totalmsg);
            }

        });

        //表格最後一欄是空白，不要顯示!
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(250);

        table.setItems(order_list);

        //把漢堡菜單填入產品窗格
        menuPane.getChildren().clear();
        menuPane.getChildren().add(getCategoryMenu("漢堡"));

        System.out.println("OK");

    }

    //取得飲料種類的菜單:引數是--果汁 茶飲 咖啡
    public GridPane getCategoryMenu(String category) {

        //準備一個格子窗格
        GridPane menuGrid = new GridPane();
        menuGrid.setHgap(10); //設定間隔距離
        menuGrid.setVgap(10);

        int column = 0;
        int row = 0;

        for (String prod_id : product_dict.keySet()) {

            System.out.println("prod_id:" + prod_id);
            System.out.println("prod_category:" + product_dict.get(prod_id).getCategory());

            if (product_dict.get(prod_id).getCategory().equals(category)) {

                AnchorPane productPane = getProductItemPane(product_dict.get(prod_id));

                //置放3個行(欄)
                if (column == 2) {
                    column = 0;
                    row++; //3行放完才換下一個列
                }
                menuGrid.add(productPane, column++, row); // (0,0)  (1,0)  (2,0)前面是行編號後面是列編號 <--第0個row
            }
        }

        //ScrollPane scrollPane = new ScrollPane(new AnchorPane(menuGrid));
        //scrollPane.setStyle("-fx-background-color:transparent;");
        //return menuPane;
        //return scrollPane;
        return menuGrid;

    }

//這是產品菜單的小窗格 每個產品有一個專屬的窗格物件
    public AnchorPane getProductItemPane(Product product) {

        //準備一個茅點窗格
        AnchorPane itemPane = new AnchorPane();

        String css
                = "-fx-background-color: #FFFFFF;"
                + " -fx-background-radius: 0;"
                + "-fx-font-size: 20px;";

        itemPane.setStyle(css); //套用樣式

        //顯示產品名稱與價格
        Label nameLabel = new Label(product.getName());
        Label priceLabel = new Label("$" + product.getPrice());
        Label disLabel = new Label(product.getDescription());

        VBox hb_name_price = new VBox();
        hb_name_price.setAlignment(Pos.BASELINE_LEFT);
        hb_name_price.setSpacing(10);
        hb_name_price.getChildren().addAll(nameLabel, disLabel, priceLabel);

        //放圖片
        ImageView image = new ImageView();
        image.setFitWidth(150);
        image.setFitHeight(100);
        //放"+"圖片
        ImageView image2 = new ImageView();
        image2.setFitWidth(30);
        image2.setFitHeight(30);

        //幾種讀圖檔的方法   Stream是InputStream輸入串流
        Image img = new Image(getClass().getResourceAsStream("/imgs_M/" + product.getPhoto()));
        //Image image = new Image(getClass().getResource("/imgs/" + product.getImgSrc()).toString());
        //Image image = new Image("/imgs/" +product.getImgSrc()); //不建議
        image.setImage(img);

        Image img2 = new Image(getClass().getResourceAsStream("/imgs_M/擷取.PNG"));
        image2.setImage(img2);

        HBox picture = new HBox();
        picture.setAlignment(Pos.BOTTOM_RIGHT);
        picture.getChildren().addAll(image, image2);

        HBox vbox_product = new HBox();
        vbox_product.getChildren().addAll(hb_name_price, picture);
        vbox_product.setPadding(new Insets(10, 10, 10, 10));
        vbox_product.setPrefSize(450, 100);

        itemPane.getChildren().addAll(vbox_product);
        DropShadow ds1 = new DropShadow();
        ds1.setOffsetY(10.0f);
        ds1.setOffsetX(10.0f);
        ds1.setColor(Color.GAINSBORO);
        itemPane.setEffect(ds1);

        //定義滑鼠事件mouse event滑鼠點下去
        itemPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectedItem = product; //放入購物籃會用到
                showSelectedItem(product);
                add_to_cart_from_Button();
            }
        });

        return itemPane;
    }

    public void showSelectedItem(Product product) {
        //顯示產品名稱價格與圖片
        item_name.setText(product.getName());
        item_price.setText("$" + product.getPrice());
        Image image = new Image(getClass().getResource("/imgs_M/" + product.getPhoto()).toString());
        item_image.setImage(image);

    }

    private void add_to_cart_from_Button() {
        String prod_id = selectedItem.getProduct_id();
        System.out.println(prod_id); //印出看看哪個產品加入購物車

        //檢查購物車內是否有這項產品
        boolean duplication = false;

        for (OrderDetail od : order_list) {
            if (od.getProduct_id().equals(prod_id)) {
                //拿到訂購數量幾杯?
                int qty = od.getQuantity().getSelectionModel().getSelectedIndex() + 1;
                System.out.println(qty);
                od.setQuantity(qty + 1);
                duplication = true;
                
                table.getSelectionModel().select(order_list.indexOf(od));  
                
                
                
                 int row_num = table.getSelectionModel().getSelectedIndex();
                System.out.println("row_num22222 ：" + row_num);

                if (row_num != copy_row_forRightSide) {
                    if (copy_row_forRightSide != -1) {
                        OrderDetail target = (OrderDetail) table.getItems().get(copy_row_forRightSide);   //
                        target.getQuantity().setDisable(true);
                    } 
                }
                OrderDetail target = (OrderDetail) table.getItems().get(row_num);   //取得選定那一行的物件
                target.getQuantity().setDisable(false);
                copy_row_forRightSide = row_num;
                
                
 
            }
        }

        if (duplication) {
            System.out.println(prod_id + "已經加入購物車");
        } else {

            //購物車加入現在點選的品項
            OrderDetail newOrdd = new OrderDetail();
            newOrdd.setProduct_id(product_dict.get(prod_id).getProduct_id());
            newOrdd.setProduct_name(product_dict.get(prod_id).getName());
            newOrdd.setProduct_price(product_dict.get(prod_id).getPrice());

            newOrdd.getQuantity().setDisable(true);
            newOrdd.getQuantity().setOnAction((event) -> {
                OrderDetail orderdetail = table.getSelectionModel().getSelectedItem();
                //System.out.println(orderdetail.getQuantity().getSelectionModel().getSelectedIndex() + 1);
                String totalmsg = String.format("%s %d\n", "總金額:", check_total());
                display.setText(totalmsg);
            });

            newOrdd.setQuantity(1);  // 不是index 是真正選定的數值
            order_list.add(newOrdd);
            System.out.println(prod_id);
        }

        //顯示總金額於display
        String totalmsg = String.format("%s %d\n", "總金額:", check_total());
        display.setText(totalmsg);

    }

    //計算總金額
    public int check_total() {
        double total = 0;
        //將所有的訂單顯示在表格   計算總金額
        for (OrderDetail od : order_list) {
            //加總
            total += od.getProduct_price() * (od.getQuantity().getSelectionModel().getSelectedIndex() + 1);
        }

        return (int) total;

    }

    @FXML
    private void delete_row(ActionEvent event) {
        Object selectedItem = table.getSelectionModel().getSelectedItem();
        table.getItems().remove(selectedItem);
        check_total();
        //顯示總金額於display
        String totalmsg = String.format("%s %d\n", "總金額:", check_total());
        display.setText(totalmsg);

    }

//訂單刪除
    @FXML
    private void delete_order(ActionEvent event) {
        display.setText("");
        order_list.clear();
        //顯示總金額於display
        String totalmsg = String.format("%s %d\n", "總金額:", check_total());
        display.setText(totalmsg);

    }

    //結帳*******************這裡寫入訂單明細到資料庫
    @FXML
    private void check(ActionEvent event) throws Exception {
        display.setText("已結帳，發票列印中...\n");

        //呼叫新視窗
        Ask_Name_Address.AskFXMain askfx = new AskFXMain();
        askfx.showWindow();
    }

//選擇飲料菜單種類
    @FXML
    private void select_menu(ActionEvent event) {

        String category = ((Button) event.getSource()).getText();

        menuPane.getChildren().clear();//先刪除原有的窗格再加入新的類別窗格
        if (category.equals("漢堡")) {
            menuPane.getChildren().addAll(getCategoryMenu("漢堡"));
        } else if (category.equals("點心")) {
            menuPane.getChildren().addAll(getCategoryMenu("點心"));
        } else if (category.equals("飲料")) {
            menuPane.getChildren().addAll(getCategoryMenu("飲料"));
        }
    }

    public static SecondController getInstance() {
        return Second;
    }

}

package mis.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import mis.models.Order;
import mis.models.OrderDAO;
import mis.models.OrderDetail;
import mis.models.Product;
import mis.models.ProductDAO;
import mis.models.User;
import mis.models.UserDAO;
import mis.models.UserInfoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AppOrderController {

    //***資料庫操作物件
    private final ProductDAO productDao = new ProductDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final UserDAO userDao = new UserDAO();
    private final UserInfoDAO userInfoDao = new UserInfoDAO();

    //讓SpringBoot幫我們取得session物件
    @Autowired
    private HttpSession httpSession;

    @RequestMapping("/98")
    public String home(Model model) {

        // 產品首頁顯示的產品 可以選定一個產品類別或是全部
        // List<Product> products = productDao.findByCate("果汁"); //只有果汁類
        List<Product> products = productDao.getAllProducts(); //全部

        model.addAttribute("products", products);
        return "home.html";
    }

    @RequestMapping("/searchProductByCate")
    public String searchByCate(@RequestParam(name = "category") String cate, Model model) {

        System.out.println("cate:" + cate);
        List<Product> products = productDao.findByCate(cate);
        model.addAttribute("products", products);
        return "home.html";
    }

    @RequestMapping("/searchProductByName")
    public String searchByName(@RequestParam("name") String name, Model model) {

        List<Product> products = productDao.findByNameContaining(name);
        model.addAttribute("products", products);
        return "home.html";
    }

    @RequestMapping("/searchProductByPrice")
    public String searchByPrice(@RequestParam(name = "price") String strprice, Model model) {

        int price = 0;
        try {
            price = Integer.parseInt(strprice);
        } catch (NumberFormatException ex) {
            System.out.println("價格輸入格式錯誤");
            System.out.println(ex);
            return "redirect:" + "/98";  //跳到到根路徑網頁  等於回到首頁的意思         
        }

        List<Product> products = productDao.findByPriceLessThanEqual(price);
        model.addAttribute("products", products);
        return "home.html";
    }

// 將這項產品與其數量加入購物車
    @RequestMapping("/addToCart")
    public String addToCart(@ModelAttribute(name = "prod_id") String prod_id, @ModelAttribute(name = "quantity") String quantity) {
        System.out.println("orderQty:" + quantity);
        System.out.println("product_id:" + prod_id);

        // 宣告置放訂單明細的購物車cart物件
        List<OrderDetail> cart;

        //如果購物車是空的，則需要初始化一個購物車
        //如果session內就有購物車物件，就取得它，並檢查有沒有重複買，沒有就在購物車中加上這項產品
        if (httpSession.getAttribute("cart") == null) {
            cart = new ArrayList<>();
        } else {
            cart = (List<OrderDetail>) httpSession.getAttribute("cart");
        }

        Product prod = productDao.findById(prod_id);
        System.out.println(prod_id);

        //檢查購物車List是否已經有這項產品
        boolean duplication = false;
        for (OrderDetail ord : cart) {
            if (ord.getProduct_id().equals(prod_id)) {
                duplication = true;
            }
        }

        if (duplication) {
            System.out.println(prod_id + "已經加入購物車");
        } else {
            //沒有重複的產品，就加入新的這一項產品
            OrderDetail ordCart = new OrderDetail();
            ordCart.setProduct_id(prod_id);
            ordCart.setProduct_name(prod.getName());
            ordCart.setProduct_price(prod.getPrice());
            ordCart.setQuantity(Integer.parseInt(quantity));

            cart.add(ordCart); //加入購物車List

        }

        //session的購物車設定為新購物車
        httpSession.setAttribute("cart", cart);

        //計算總價 並在session物件中更新總價sum
        int sum = check_total(cart);
        System.out.println("sum:" + sum);
        httpSession.setAttribute("sum", sum);
        return "redirect:" + "/98";  //跳到到根路徑網頁  等於回到首頁的意思         

    }
//參數使用List最好，物件多型可以通吃喔! 
    //請試試看改成ArrayList或是ObservableList

    public Integer check_total(List<OrderDetail> list) {
        double total = 0;
        //將所有的訂單顯示在表格   計算總金額
        for (OrderDetail od : list) {
            //加總
            total += od.getProduct_price() * od.getQuantity();
        }

        //顯示總金額於display
        String total_msg = String.format("%s %d\n", "總金額:", Math.round(total));
        //display.setText(total_msg);
        System.out.println(total_msg);

        return (int) total;

    }

    // 修改購物車內項目的訂購數量
    @RequestMapping("/modify")
    public String modifyCart(@RequestParam String prod_id, @RequestParam("orderQty") String orderQty) {

        //取得購物車內容
        List<OrderDetail> cart = (List<OrderDetail>) httpSession.getAttribute("cart");

        //判斷哪一筆要修改數量
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct_id().equals(prod_id)) {
                //找到該項產品 數量更新為orderQty值 後面的迴圈就跳開不必做了
                cart.get(i).setQuantity(Integer.parseInt(orderQty));
                break;
            }
        }
        System.out.println("orderQty:" + orderQty);

        //更新session的內容
        httpSession.setAttribute("cart", cart);
        int sum = check_total(cart);
        httpSession.setAttribute("sum", sum);

        return "redirect:/cart";
    }

// 刪除購物車項目
    @RequestMapping("/delete")
    public String deleteFromCart(@RequestParam("prod_id") String prod_id) {

        List<OrderDetail> cart = (List<OrderDetail>) httpSession.getAttribute("cart");

        // 找到欲刪除的項目，刪除之
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct_id().equals(prod_id)) {
                cart.remove(i);
                break;
            }
        }

        //更新session的內容
        httpSession.setAttribute("cart", cart);

        int sum = check_total(cart);
        httpSession.setAttribute("sum", sum);

        return "redirect:/cart";
    }

    // 顯示購物車內容
    @RequestMapping("/cart")
    public String cart() {

        //若還沒有購物車屬性 則在sum的值設定"尚未購物或購物車已過期"，並渲染購物車網頁
        if (httpSession.getAttribute("cart") == null) {
            httpSession.setAttribute("sum", "尚未購物或購物車已過期");
        }
        return "cart.html";
    }

    // 結帳 
    @RequestMapping("/check")
    public String check() {

        return "check.html";
    }

    // 將訂單明細存檔資料庫
    @RequestMapping("/save")
    public String SaveCart(
            @RequestParam(name = "user-name", required = false, defaultValue = "無姓名") String userName,
            @RequestParam(name = "phone", required = false, defaultValue = "無電話") String phone,
            @RequestParam(name = "address", required = false, defaultValue = "無地址") String address
    ) {

        //取得購物車
        List<OrderDetail> cart = (List<OrderDetail>) httpSession.getAttribute("cart");

        //如果購物車是空的回到首頁會回到購物車
        if (cart.isEmpty()) {
            return "redirect:/98";
        }

        //這裡要取得不重複的order_num，加1之後當作訂單的流水號
        String order_num = orderDao.getMaxOrderNum();

        if (order_num == null) {
            order_num = "ord-100";
        }

        System.out.println(order_num);
        System.out.println(order_num.split("-")[1]);
        //String order_num = "ord-102";
        int serial_num = Integer.parseInt(order_num.split("-")[1]) + 1;
        System.out.println(serial_num);

        String new_order_num = "ord-" + serial_num;

        int sum = check_total(cart);
        //Cart ord = new Cart(new_order_num, "2021-05-01", 123, userName);

        //初始劃一個Order物件寫入資料庫
        Order ord = new Order();
        ord.setOrder_num(new_order_num);
        ord.setTotal_price(sum);
        ord.setCustomer_name(userName);
        ord.setCustomer_phtone(phone);
        ord.setCustomer_address(address);

        orderDao.insertCart(ord);//寫入資料庫

        // 將訂單明細物件一一寫入資料庫
        for (int i = 0; i < cart.size(); i++) {
            OrderDetail item = new OrderDetail();
            item.setOrder_num(new_order_num);
            item.setProduct_id(cart.get(i).getProduct_id());
            item.setQuantity(cart.get(i).getQuantity());
            item.setProduct_price(cart.get(i).getProduct_price());
            item.setProduct_name(cart.get(i).getProduct_name());

            orderDao.insertCartItem(item);//寫入資料庫
        }

        //清空session內的購物車的內容
        cart = new ArrayList<>(); //清空cart
        httpSession.setAttribute("cart", cart);

        sum = 0;
        httpSession.setAttribute("sum", sum);

        return "check2.html";
        //return "redirect:/";
    }

    // 登入系統
        //驗證登入
    @RequestMapping(value = "/auth-login", method = RequestMethod.POST)
    public String auth_login(@ModelAttribute(name = "email") String email, @ModelAttribute(name = "password") String password) {
        User user = new User();
        if (email.equals("") || password.equals("")) {
            httpSession.setAttribute("msg", "請輸入帳號或密碼");
            return "redirect:/login";
        } else {
            user.setEmail(email);
            user.setPassword(password);
        }

        Map<Boolean, String> confirm = userDao.confirm(user);
        if (confirm.containsKey(true)) {
            httpSession.setAttribute("loginConfirm", true);
            httpSession.setAttribute("msg", confirm.get(true));
            httpSession.setAttribute("user_id", userDao.getIdFromUser(user));
            httpSession.setAttribute("permission", userDao.getPermissionFromUser(user));
            return "redirect:/98";
        } else {
            String error = confirm.get(false);
            httpSession.setAttribute("loginConfirm", false);
            httpSession.setAttribute("msg", error);
            return "redirect:/login";
        }

    }
//登入介面
    @RequestMapping("/login")
    public String login() {
        if (httpSession.getAttribute("register") != null) {
            if (httpSession.getAttribute("register").equals(false)) {
                httpSession.setAttribute("msg", null);
                httpSession.setAttribute("register", null);
            }
        }
        return "login.html";
    }
//註冊介面
    @RequestMapping("/register")
    public String register() {
        if (httpSession.getAttribute("register") != null) {
            if (httpSession.getAttribute("register").equals(true)) {
                httpSession.setAttribute("msg", null);
                httpSession.setAttribute("register", null);
            }
        }
        if (httpSession.getAttribute("loginConfirm") != null) {
            if (httpSession.getAttribute("loginConfirm").equals(false)) {
                httpSession.setAttribute("msg", null);
                httpSession.setAttribute("loginConfirm", null);
            }
        }
        return "register.html";
    }
    
    //註冊的驗證
     @RequestMapping(value = "/auth-register", method = RequestMethod.POST)
    public String auth_register(@ModelAttribute(name = "email") String email, @ModelAttribute(name = "password") String password, @ModelAttribute(name = "confirm-password") String confirm_password)
    {
        if (email.equals(""))
        {
            httpSession.setAttribute("msg", "帳號不可為空");
            httpSession.setAttribute("register",false);
            return "redirect:/register";
        }
        else if (!email.contains("@"))
        {
            httpSession.setAttribute("msg", "請輸入正確電子郵件格式");
            httpSession.setAttribute("register",false);
            return "redirect:/register";
        }
        if (password.equals("") || confirm_password.equals(""))
        {
            httpSession.setAttribute("msg", "密碼不可為空");
            httpSession.setAttribute("register",false);
            return "redirect:/register";
        }
        else if (!password.equals(confirm_password))
        {
            httpSession.setAttribute("msg", "重複輸入密碼不一致，請重新註冊");
            httpSession.setAttribute("register",false);
            return "redirect:/register";
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        Map<Boolean, String> confirm = userDao.insertUserAccount(user);
        if (confirm.containsKey(true))
        {
            httpSession.setAttribute("msg", confirm.get(true));
            httpSession.setAttribute("register",true);
            return "redirect:/login";
        }
        else
        {
            httpSession.setAttribute("msg", confirm.get(false));
            httpSession.setAttribute("register",false);
            return "redirect:/register";
        }

    }
    
    @RequestMapping("/logout")
    public String logout(HttpSession session)
    {
        session.invalidate();
        return "redirect:/98";
    }
    
 
}

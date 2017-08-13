# 酷嘭！！(coupon)

生活的“优惠券”，让生活更酷嘭！！

# document of api

* update user's coupon

``` 
PUT /coupons
Content-Type: text/edn;
body {:username "logged username(String)"
      :member-name "owner of the coupon(String)"
      :group-id "the group id(String of UUID)
      :coupon-id "the coupon id(String of UUID)
      :change-num 1 (int)"}
      
response:
{:status-code 200
 :body {:result :success :data {:username "logged username(String)"
                                     :member-name "owner of the coupon(String)"
                                     :group-id "the group id(String of UUID)
                                     :coupon-id "the coupon id(String of UUID)
                                     :change-num 1 (int)"}}}}
eg:
 curl -X PUT --header "Content-Type:text/edn" \
 -d "{:username \"柳朕\", :member-name \"姜琳琳\", \
 :group-id \"bf67756e-ecb7-4eb3-86df-ec153bf03f22\", \
 :coupon-id \"08ce1f86-e37a-44f6-89c8-de32f0509b00\", \
 :change-num 1}" \
 "http://127.0.0.1:8088/coupons"                               
                                
```



## License

Copyright © Snailiu

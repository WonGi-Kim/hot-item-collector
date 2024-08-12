import { createRouter, createWebHistory } from 'vue-router';
import MainPage from '@/components/MainPage.vue';
import ConfirmPassword from "@/components/ConfirmPassword.vue";
import MyProfilePage from "@/components/MyProfilePage.vue";
import FollowList from "@/components/FollowList.vue";
import DetailProductPage from "@/components/DetailProductPage.vue";
import CartPage from "@/components/CartPage.vue";
import OrderPage from "@/components/OrderPage.vue";
import ManageOrder from "@/components/ManageOrder.vue";
import DetailOrder from "@/components/DetailOrder.vue";
import ProductUpdate from "@/components/ProductUpdate.vue";
import SearchPage from "@/components/SearchPage.vue";
import ProfileUpdate from "@/components/ProfileUpdate.vue";
import OrderItem from "@/components/OrderItem.vue";
import OtherProfilePage from "@/components/OtherProfilePage.vue";
import UploadProduct from "@/components/UploadProduct.vue";
import SaleProductPage from "@/components/SaleProductPage.vue";
import FollowItemPage from "@/components/FollowItemPage.vue";
import NewItemPage from "@/components/NewItemPage.vue";
import LikeItemPage from "@/components/LikeItemPage.vue";
import CategoryItemPage from "@/components/CategoryItemPage.vue";
import EditProduct from "@/components/EditProduct.vue";
import OAuthSignupPage from "@/components/OAuthSignupPage.vue";

const routes = [
  {
    path: '/',
    name: 'MainPage',
    component: MainPage,
  },
  {
    path: '/oauth2/signup',
    name: 'OAuthSignupPage',
    component: OAuthSignupPage,
  },
  {
    path: '/profile/update/password',
    name: 'ConfirmPassword',
    component: ConfirmPassword,
  },
  {
    path: '/profile',
    name: 'MyProfilePage',
    component: MyProfilePage,
  },
  {
    path: '/profile/follow',
    name: 'FollowList',
    component: FollowList,
  },
  {
    path: '/product/detail/:productId',
    name: 'DetailProductPage',
    component: DetailProductPage,
    props: true
  },
  {
    path: '/cart',
    name: 'CartPage',
    component: CartPage,
  },
  {
    path: '/order',
    name: 'OrderPage',
    component: OrderPage,
  },
  {
    path: '/orders/sell',
    name: 'ManageOrder',
    component: ManageOrder,
  },
  {
    path: '/order/detail',
    name: 'DetailOrder',
    component: DetailOrder,
  },
  {
    path: '/product/update/:productId',
    name: 'ProductUpdate',
    component: ProductUpdate,
    props: true
  },
  {
    path: '/search',
    name: 'SearchPage',
    component: SearchPage,
  },
  {
    path: '/profile/update',
    name: 'ProfileUpdate',
    component: ProfileUpdate,
  },
  {
    path: '/orders/buy',
    name: 'OrderItem',
    component: OrderItem,
  },
  {
    path: '/seller/:userId',
    name: 'OtherProfilePage',
    component: OtherProfilePage,
    props: true
  },
  {
    path: '/product/upload',
    name: 'UploadProduct',
    component: UploadProduct,
  },
  {
    path: '/product/sale',
    name: 'SaleProductPage',
    component: SaleProductPage,
  },
  {
    path: '/followed-items',
    name: 'FollowItemPage',
    component: FollowItemPage,
  },
  {
    path: '/category-items',
    name: 'CategoryItemPage',
    component: CategoryItemPage,
  },
  {
    path: '/new-items',
    name: 'NewItemPage',
    component: NewItemPage,
  },
  {
    path: '/product/like',
    name: 'LikeItemPage',
    component: LikeItemPage,
  },
  {
    path: '/product/edit/:productId',
    name: 'EditProduct',
    component: EditProduct,
    props: true
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;

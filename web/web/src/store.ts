import { reactive } from 'vue';

// 开发环境自动登录开关（预览时保持 true，联调后端时改为 false）
const AUTO_LOGIN = true;

// 初始化时根据开关决定是否注入假用户
const initId = AUTO_LOGIN ? '1' : localStorage.getItem('userId') || '';
const initUsername = AUTO_LOGIN ? '测试用户' : localStorage.getItem('username') || '';
const initRole = AUTO_LOGIN ? 'USER' : localStorage.getItem('role') || '';
const initToken = AUTO_LOGIN ? 'mock-token-default' : localStorage.getItem('token') || '';

export const userState = reactive({
  id: initId,
  username: initUsername,
  role: initRole,
  token: initToken,

  login(id: string, username: string, role: string, token: string) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.token = token;
    localStorage.setItem('userId', id);
    localStorage.setItem('username', username);
    localStorage.setItem('role', role);
    localStorage.setItem('token', token);
  },

  logout() {
    this.id = '';
    this.username = '';
    this.role = '';
    this.token = '';
    localStorage.clear();
    // 如果希望退出后仍然保持登录状态（因为开启了自动登录），可以取消下面注释
    // if (AUTO_LOGIN) {
    //   this.id = '1';
    //   this.username = '测试用户';
    //   this.role = 'USER';
    //   this.token = 'mock-token-default';
    // }
  }
});
import { reactive } from 'vue';

export const userState = reactive({
  id: localStorage.getItem('userId') || '',
  username: localStorage.getItem('username') || '',
  role: localStorage.getItem('role') || '',
  token: localStorage.getItem('token') || '',

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
  }
});

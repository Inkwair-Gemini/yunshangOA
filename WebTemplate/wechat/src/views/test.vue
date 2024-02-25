<template>
  <div>
    <div>账号切换</div>
    <button @click="nbg()" type="default" size="mini">牛逼哥</button>
    <button @click="lisi()" type="default" size="mini">李四</button>
    <button @click="zhangsan()" type="default" size="mini">张三</button>
    <button @click="admin()" type="default" size="mini">admin</button>
    <div>当前token：{{ token }}</div>
  </div>
</template>

<script>
export default {
  name: "Test",

  data() {
    return {
      token: ''
    };
  },

  created(){
    this.token = window.localStorage.getItem('token')
  },

  methods: {
    admin() {
      window.localStorage.setItem('token', '');
      let token = 'eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAA_6tWKi5NUrJScgwN8dANDXYNUtJRSq0oULIyNLMwMzcyNjA11lEqLU4t8kwBikGYeYm5qUAtiSm5mXlKtQCAvhdVQgAAAA.tRgdubU3xzi2Rhi0rKIt07P3Hp25ysOfczZ_x6A8m9OHm9FA9cdRlB8-JdjtkWt54hWh-I1v_Nl9bKh2XberxA'
      window.localStorage.setItem('token', token);
      this.token = window.localStorage.getItem('token')
    },

    nbg() {
      window.localStorage.setItem('token', '');
      let token = 'eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJScgwN8dANDXYNUtJRSq0oULIyNLMwMTA2sLA00FEqLU4t8kwBihlD2HmJualAPSmJSZmJ-Uq1AG68LJdEAAAA.kU_5_px7oYGuswBlvaDVKuc_MXEtCFxrdfW6kEqeuK53Aj6n_Uh_LS0tuA7QL3SzmO7BOvKWhv8_t6tNYL5yhg'
      window.localStorage.setItem('token', token);
      this.token = window.localStorage.getItem('token')
    },

    zhangsan() {
      window.localStorage.setItem('token', '');
      let token = 'eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAA_6tWKi5NUrJScgwN8dANDXYNUtJRSq0oULIyNLMwMzcyNrI01lEqLU4t8kxRsjKHMPMSc1OBWqoyEvPSixPzlGoBdQcznUUAAAA.ZkJkCc5sCXr_bJcrpwPuNdDax1fOCgbacOU6G9EalSUTQiFnf6Myac85oWsnP0n2eIDbUIQePYJkJSkc-10rOA'
      window.localStorage.setItem('token', token);
      this.token = window.localStorage.getItem('token')
    },

    lisi() {
      window.localStorage.setItem('token', '');
      let token = 'eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAA_6tWKi5NUrJScgwN8dANDXYNUtJRSq0oULIyNLMwMzcyMTYz11EqLU4t8kxRsjKBMPMSc1OBWnIyizOVagEwGbAzQQAAAA.3LZanALj8oigVnq2lZIJCWjffiOQCIzbKCJJSyKgkE-8THOHmvN0dAJ7h7yRBBKDKxy76gMQ_4KIwulcas1Aag'
      window.localStorage.setItem('token', token);
      this.token = window.localStorage.getItem('token')
    }
  }
}
</script>

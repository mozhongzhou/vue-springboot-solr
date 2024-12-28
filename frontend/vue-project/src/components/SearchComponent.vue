<template>
  <div class="search-container">
    <!-- 连接状态检查 -->
    <div
      class="connection-status"
      :class="{ connected: isConnected, disconnected: !isConnected }"
    >
      Solr服务状态: {{ isConnected ? "已连接" : "未连接" }}
    </div>

    <!-- 搜索区域 -->
    <div class="search-box">
      <input
        v-model="query"
        placeholder="请输入搜索关键词..."
        @keyup.enter="search"
        :disabled="!isConnected"
        class="search-input"
      />
      <button
        @click="search"
        :disabled="!isConnected || !query"
        class="search-button"
      >
        <span v-if="!loading">搜索</span>
        <span v-else>搜索中...</span>
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- 搜索结果 -->
    <div v-if="results && results.length > 0" class="search-results">
      <div v-for="result in results" :key="result.id" class="result-card">
        <h3 v-if="result.company" v-html="result.company"></h3>
        <div v-if="result.address" class="result-field">
          <strong>地址：</strong><span v-html="result.address"></span>
        </div>
        <div v-if="result.type" class="result-field">
          <strong>类型：</strong><span v-html="result.type"></span>
        </div>
        <div v-if="result.business" class="result-field">
          <strong>业务：</strong><span v-html="result.business"></span>
        </div>
        <div v-if="result.introduce" class="result-field">
          <strong>简介：</strong><span v-html="result.introduce"></span>
        </div>
      </div>
    </div>

    <!-- 无结果提示 -->
    <div v-else-if="results && !loading && query && !error" class="no-results">
      未找到相关结果
    </div>
  </div>
</template>

<script>
import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

export default {
  name: "SearchComponent",
  data() {
    return {
      query: "",
      results: null,
      loading: false,
      error: null,
      isConnected: false,
    };
  },
  created() {
    this.checkConnection();
  },
  methods: {
    async checkConnection() {
      try {
        const response = await axios.get(`${API_BASE_URL}/test-connection`, {
          withCredentials: true,
        });
        this.isConnected = response.data.status === "success";
      } catch (error) {
        this.isConnected = false;
        this.error = "无法连接到搜索服务";
        console.error("Connection error:", error);
      }
    },
    async search() {
      if (!this.query || !this.isConnected) return;

      this.loading = true;
      this.error = null;

      try {
        const response = await axios.get(`${API_BASE_URL}/search`, {
          params: {
            q: this.query,
          },
          withCredentials: true,
        });
        this.results = response.data.results;
      } catch (error) {
        this.error = "搜索过程中出现错误，请稍后重试";
        console.error("Search error:", error);
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.connection-status {
  padding: 8px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
  text-align: center;
}

.connected {
  background-color: #e6f4ea;
  color: #1e7e34;
}

.disconnected {
  background-color: #fde7e9;
  color: #dc3545;
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #4a90e2;
  outline: none;
}

.search-button {
  padding: 12px 24px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.search-button:hover:not(:disabled) {
  background-color: #357abd;
}

.search-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  padding: 12px;
  background-color: #fde7e9;
  color: #dc3545;
  border-radius: 4px;
  margin-bottom: 20px;
}

.search-results {
  display: grid;
  gap: 20px;
}

.result-card {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.result-card h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
}

.result-field {
  margin-bottom: 8px;
  line-height: 1.5;
}

.result-field strong {
  color: #666;
  margin-right: 8px;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #666;
  background-color: #f8f9fa;
  border-radius: 8px;
}

:deep(em) {
  background-color: #fff3cd;
  font-style: normal;
  padding: 0 2px;
  border-radius: 2px;
}
</style>

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS ai_knowledge_base (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  content      TEXT,
  metadata     JSONB,
  embedding    VECTOR(768)
);

CREATE INDEX IF NOT EXISTS ai_kb_embedding_idx
  ON ai_knowledge_base USING hnsw (embedding vector_cosine_ops);

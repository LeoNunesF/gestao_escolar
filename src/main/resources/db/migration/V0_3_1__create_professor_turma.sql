-- Migration: Create professor_turma table for managing professor-class assignments
-- This table creates a many-to-many relationship between professors and classes (turmas)
-- Note: Adjust FK table names if they differ in your schema (e.g., 'professor' vs 'professores', 'turma' vs 'turmas')

CREATE TABLE professor_turma (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professor_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    papel VARCHAR(50),
    disciplina VARCHAR(100),
    data_inicio DATE,
    data_termino DATE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint to prevent duplicate assignments
    CONSTRAINT uk_professor_turma UNIQUE (professor_id, turma_id),
    
    -- Foreign key constraints
    -- Adjust table names if necessary (professores vs professor, turmas vs turma)
    CONSTRAINT fk_professor_turma_professor FOREIGN KEY (professor_id) REFERENCES professores(id),
    CONSTRAINT fk_professor_turma_turma FOREIGN KEY (turma_id) REFERENCES turmas(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_professor_turma_professor ON professor_turma(professor_id);
CREATE INDEX idx_professor_turma_turma ON professor_turma(turma_id);
CREATE INDEX idx_professor_turma_ativo ON professor_turma(data_termino);

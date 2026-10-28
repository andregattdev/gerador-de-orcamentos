package com.app.gerador_de_orcamento.model;

public enum StatusServico {
    RASCUNHO,      // O prestador está montando os valores
    APROVADO,      // O cliente aceitou, aguardando começar/agendado
    EM_ANDAMENTO,  // Prestador está executando o serviço
    FINALIZADO,    // Serviço pronto, aguardando o pagamento do cliente
    CONCLUIDO,      // Pago e encerrado! Dinheiro no bolso.
    CANCELADO,     // O cliente desistiu ou o prestador cancelou
    REJEITADO      // O cliente rejeitou a proposta, aguardando nova proposta
}

(() => {
    const configEl = document.getElementById('checkout-config');
    if (!configEl) {
        return;
    }

    const pagamentoId = configEl.dataset.pagamentoId;
    let statusAtual = configEl.dataset.status;
    let segundosRestantes = Number.parseInt(configEl.dataset.segundosRestantes || '0', 10);
    const totalSegundosJanela = 600;

    // Elements
    const timerPill = document.getElementById('timer-pill');
    const timerText = document.getElementById('timer-text');
    const countdownBar = document.getElementById('countdown-bar-fill');
    const statusBadge = document.getElementById('summary-status-badge');

    const formSection = document.getElementById('checkout-form-section');
    const resultSection = document.getElementById('checkout-result-section');
    const globalErrorBox = document.getElementById('global-error-box');

    const tabButtons = document.querySelectorAll('.method-tab');
    const cardFieldsPanel = document.getElementById('card-fields-panel');
    const pixFieldsPanel = document.getElementById('pix-fields-panel');

    const cardNumberInput = document.getElementById('numeroCartao');
    const cardHolderInput = document.getElementById('nomeTitular');
    const cardExpiryInput = document.getElementById('validade');
    const cardCvvInput = document.getElementById('cvv');

    const previewNumber = document.getElementById('preview-number');
    const previewHolder = document.getElementById('preview-holder');
    const previewExpiry = document.getElementById('preview-expiry');
    const previewMethod = document.getElementById('preview-method');

    const submitCardBtn = document.getElementById('submit-card-btn');
    const submitPixBtn = document.getElementById('submit-pix-btn');
    const copyPixBtn = document.getElementById('copy-pix-btn');
    const pixCodeInput = document.getElementById('pix-payload-input');
    const simulateExpireBtn = document.getElementById('simulate-expire-btn');
    const sandboxToolsBox = document.getElementById('sandbox-tools-box');

    let metodoSelecionado = 'CARTAO_CREDITO';
    let processando = false;
    let timerInterval = null;
    let syncInterval = null;

    function formatarTempo(segundos) {
        const clamped = Math.max(0, segundos);
        const min = String(Math.floor(clamped / 60)).padStart(2, '0');
        const sec = String(clamped % 60).padStart(2, '0');
        return `${min}:${sec}`;
    }

    function atualizarTimerVisual() {
        if (!timerPill || !timerText) return;

        if (statusAtual === 'APROVADO') {
            timerPill.className = 'timer-pill done';
            timerText.textContent = 'Pago';
            if (countdownBar) countdownBar.style.width = '100%';
            return;
        }

        if (statusAtual === 'EXPIRADO' || statusAtual === 'RECUSADO' || segundosRestantes <= 0) {
            timerPill.className = 'timer-pill expired';
            timerText.textContent = statusAtual === 'RECUSADO' ? 'Encerrado' : '00:00 Expirado';
            if (countdownBar) countdownBar.style.width = '0%';
            return;
        }

        timerText.textContent = formatarTempo(segundosRestantes);
        const pct = Math.min(100, Math.max(0, (segundosRestantes / totalSegundosJanela) * 100));
        if (countdownBar) {
            countdownBar.style.width = `${pct}%`;
            countdownBar.classList.toggle('warning', segundosRestantes <= 120);
        }
        timerPill.className = segundosRestantes <= 120 ? 'timer-pill warning' : 'timer-pill';
    }

    function iniciarContagemRegressiva() {
        atualizarTimerVisual();
        if (statusAtual !== 'AGUARDANDO_PAGAMENTO') {
            return;
        }

        timerInterval = setInterval(() => {
            if (statusAtual !== 'AGUARDANDO_PAGAMENTO') {
                clearInterval(timerInterval);
                return;
            }
            if (segundosRestantes > 0) {
                segundosRestantes -= 1;
                atualizarTimerVisual();
            } else {
                sincronizarStatusServidor();
            }
        }, 1000);

        // Sincroniza periodicamente com o backend (captura expiração feita pelo SchedulerJob)
        syncInterval = setInterval(() => {
            if (statusAtual === 'AGUARDANDO_PAGAMENTO' && !processando) {
                sincronizarStatusServidor();
            }
        }, 5000);
    }

    async function sincronizarStatusServidor() {
        try {
            const resp = await fetch(`/api/v1/pagamentos/${pagamentoId}`);
            if (!resp.ok) return;
            const data = await resp.json();
            segundosRestantes = data.segundosRestantesParaExpirar ?? 0;
            if (data.status && data.status !== 'AGUARDANDO_PAGAMENTO') {
                aplicarEstadoTerminal(data);
            } else {
                atualizarTimerVisual();
            }
        } catch (_) {
            // Ignora falhas transitórias de polling
        }
    }

    function selecionarMetodo(metodo) {
        metodoSelecionado = metodo;
        tabButtons.forEach((btn) => {
            btn.classList.toggle('active', btn.dataset.method === metodo);
        });

        const ehPix = metodo === 'PIX';
        cardFieldsPanel?.classList.toggle('hidden', ehPix);
        pixFieldsPanel?.classList.toggle('hidden', !ehPix);

        if (previewMethod) {
            previewMethod.textContent = metodo === 'CARTAO_DEBITO' ? 'DÉBITO' : 'CRÉDITO';
        }
        limparErros();
    }

    tabButtons.forEach((btn) => {
        btn.addEventListener('click', () => selecionarMetodo(btn.dataset.method));
    });

    // Máscaras e preview ao vivo
    function formatarNumeroCartao(valor) {
        const digitos = valor.replace(/\D/g, '').slice(0, 16);
        return digitos.replace(/(\d{4})(?=\d)/g, '$1 ');
    }

    function formatarValidade(valor) {
        const digitos = valor.replace(/\D/g, '').slice(0, 6);
        if (digitos.length <= 2) return digitos;
        return `${digitos.slice(0, 2)}/${digitos.slice(2)}`;
    }

    cardNumberInput?.addEventListener('input', (e) => {
        e.target.value = formatarNumeroCartao(e.target.value);
        if (previewNumber) {
            previewNumber.textContent = e.target.value || '•••• •••• •••• ••••';
        }
    });

    cardHolderInput?.addEventListener('input', (e) => {
        e.target.value = e.target.value.toUpperCase();
        if (previewHolder) {
            previewHolder.textContent = e.target.value || 'NOME DO TITULAR';
        }
    });

    cardExpiryInput?.addEventListener('input', (e) => {
        e.target.value = formatarValidade(e.target.value);
        if (previewExpiry) {
            previewExpiry.textContent = e.target.value || 'MM/AAAA';
        }
    });

    cardCvvInput?.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '').slice(0, 4);
    });

    // Chips de preenchimento rápido para teste de UX
    document.querySelectorAll('[data-preset-card]').forEach((chip) => {
        chip.addEventListener('click', () => {
            if (metodoSelecionado === 'PIX') {
                selecionarMetodo('CARTAO_CREDITO');
            }
            const numero = chip.dataset.presetCard;
            cardNumberInput.value = numero;
            cardHolderInput.value = 'JOAO V NOVAES';
            cardExpiryInput.value = '12/2030';
            cardCvvInput.value = '123';

            if (previewNumber) previewNumber.textContent = numero;
            if (previewHolder) previewHolder.textContent = 'JOAO V NOVAES';
            if (previewExpiry) previewExpiry.textContent = '12/2030';
            limparErros();
        });
    });

    // Copiar PIX
    copyPixBtn?.addEventListener('click', async () => {
        if (!pixCodeInput) return;
        try {
            await navigator.clipboard.writeText(pixCodeInput.value);
            const original = copyPixBtn.textContent;
            copyPixBtn.textContent = 'Copiado!';
            setTimeout(() => {
                copyPixBtn.textContent = original;
            }, 1800);
        } catch (_) {
            pixCodeInput.select();
        }
    });

    function limparErros() {
        if (globalErrorBox) {
            globalErrorBox.classList.add('hidden');
            globalErrorBox.textContent = '';
        }
        document.querySelectorAll('.field-error').forEach((el) => {
            el.textContent = '';
        });
        document.querySelectorAll('.is-invalid').forEach((el) => {
            el.classList.remove('is-invalid');
        });
    }

    function exibirErroCampo(campo, mensagem) {
        const input = document.getElementById(campo);
        const errorEl = document.getElementById(`erro-${campo}`);
        input?.classList.add('is-invalid');
        if (errorEl) {
            errorEl.textContent = mensagem;
        }
    }

    function exibirErroGlobal(mensagem) {
        if (!globalErrorBox) return;
        globalErrorBox.textContent = mensagem;
        globalErrorBox.classList.remove('hidden');
    }

    function setLoading(ativo) {
        processando = ativo;
        [submitCardBtn, submitPixBtn, simulateExpireBtn].forEach((btn) => {
            if (btn) btn.disabled = ativo;
        });

        if (submitCardBtn) {
            submitCardBtn.innerHTML = ativo
                ? '<span class="spinner"></span> Processando no gateway...'
                : 'Finalizar Pagamento';
        }
        if (submitPixBtn) {
            submitPixBtn.innerHTML = ativo
                ? '<span class="spinner"></span> Confirmando PIX...'
                : 'Confirmar Pagamento PIX';
        }
    }

    function atualizarStatusBadge(novoStatus) {
        if (!statusBadge) return;
        statusBadge.textContent = novoStatus;
        statusBadge.className = `status-badge status-${novoStatus}`;
    }

    function aplicarEstadoTerminal(data) {
        statusAtual = data.status;
        clearInterval(timerInterval);
        clearInterval(syncInterval);
        atualizarStatusBadge(data.status);
        atualizarTimerVisual();

        formSection?.classList.add('hidden');
        resultSection?.classList.remove('hidden');
        sandboxToolsBox?.classList.add('hidden');

        const panelApproved = document.getElementById('panel-aprovado');
        const panelRefused = document.getElementById('panel-recusado');
        const panelExpired = document.getElementById('panel-expirado');

        panelApproved?.classList.toggle('hidden', data.status !== 'APROVADO');
        panelRefused?.classList.toggle('hidden', data.status !== 'RECUSADO');
        panelExpired?.classList.toggle('hidden', data.status !== 'EXPIRADO');

        if (data.status === 'APROVADO') {
            const authEl = document.getElementById('result-auth-code');
            const methodEl = document.getElementById('result-metodo');
            if (authEl) authEl.textContent = data.codigoTransacaoGateway || '-';
            if (methodEl) methodEl.textContent = data.metodoPagamento || '-';
        } else if (data.status === 'RECUSADO') {
            const reasonEl = document.getElementById('result-motivo-recusa');
            if (reasonEl) reasonEl.textContent = data.motivoRecusa || 'Transação recusada pela operadora.';
        } else if (data.status === 'EXPIRADO') {
            const expReasonEl = document.getElementById('result-motivo-expirado');
            if (expReasonEl && data.motivoRecusa) {
                expReasonEl.textContent = data.motivoRecusa;
            }
        }
    }

    async function enviarPagamento() {
        if (processando || statusAtual !== 'AGUARDANDO_PAGAMENTO') {
            return;
        }
        limparErros();
        setLoading(true);

        const payload = metodoSelecionado === 'PIX'
            ? {
                metodoPagamento: 'PIX',
                numeroCartao: null,
                nomeTitular: null,
                validade: null,
                cvv: null
            }
            : {
                metodoPagamento: metodoSelecionado,
                numeroCartao: cardNumberInput?.value || '',
                nomeTitular: cardHolderInput?.value || '',
                validade: cardExpiryInput?.value || '',
                cvv: cardCvvInput?.value || ''
            };

        try {
            const response = await fetch(`/api/v1/pagamentos/${pagamentoId}/processar`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const body = await response.json().catch(() => ({}));

            if (response.ok) {
                aplicarEstadoTerminal(body);
                return;
            }

            if (response.status === 400 && body.erros) {
                Object.entries(body.erros).forEach(([campo, msg]) => {
                    exibirErroCampo(campo, msg);
                });
                exibirErroGlobal(body.detail || 'Verifique os campos destacados antes de continuar.');
                return;
            }

            if (response.status === 422) {
                aplicarEstadoTerminal({
                    status: 'EXPIRADO',
                    motivoRecusa: body.detail || 'Prazo limite de 10 minutos para pagamento foi excedido.'
                });
                return;
            }

            if (response.status === 409) {
                exibirErroGlobal(body.detail || 'Este pagamento já foi processado ou atualizado simultaneamente.');
                await sincronizarStatusServidor();
                return;
            }

            exibirErroGlobal(body.detail || 'Não foi possível concluir o pagamento. Tente novamente.');
        } catch (_) {
            exibirErroGlobal('Falha de comunicação com o servidor de pagamentos.');
        } finally {
            setLoading(false);
        }
    }

    submitCardBtn?.addEventListener('click', enviarPagamento);
    submitPixBtn?.addEventListener('click', enviarPagamento);

    simulateExpireBtn?.addEventListener('click', async () => {
        if (processando) return;
        setLoading(true);
        try {
            const resp = await fetch(`/pagamentos/sandbox/${pagamentoId}/simular-expiracao`, {
                method: 'POST'
            });
            if (resp.ok) {
                const data = await resp.json();
                aplicarEstadoTerminal(data);
            } else {
                exibirErroGlobal('Não foi possível simular a expiração desta cobrança.');
            }
        } catch (_) {
            exibirErroGlobal('Erro ao comunicar com o Sandbox.');
        } finally {
            setLoading(false);
        }
    });

    iniciarContagemRegressiva();
})();

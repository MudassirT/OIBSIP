import React from 'react';

const STEPS = [
  { key: 'Order Received', label: 'Order Received', icon: '📝', desc: 'Verified & sent to kitchen' },
  { key: 'In Kitchen', label: 'In Kitchen', icon: '👨‍🍳', desc: 'Baking at 450°C wood oven' },
  { key: 'Sent to Delivery', label: 'Out for Delivery', icon: '🛵', desc: 'Hot & fresh on the road' },
  { key: 'Delivered', label: 'Delivered', icon: '🎉', desc: 'Delivered to your doorstep' }
];

const OrderStepper = ({ currentStatus }) => {
  const currentIndex = STEPS.findIndex((s) => s.key === currentStatus);
  const activeIndex = currentIndex === -1 ? 0 : currentIndex;

  return (
    <div style={{ width: '100%', margin: '1.5rem 0' }}>
      {/* Progress Track Bar */}
      <div
        style={{
          position: 'relative',
          padding: '0 1rem'
        }}
      >
        {/* Background Grey Track */}
        <div
          style={{
            position: 'absolute',
            top: '24px',
            left: '3rem',
            right: '3rem',
            height: '4px',
            backgroundColor: '#E2E8F0',
            borderRadius: 'var(--radius-full)',
            zIndex: 1
          }}
          className="hide-on-mobile"
        >
          {/* Active Colored Fill Bar */}
          <div
            style={{
              height: '100%',
              width: `${(activeIndex / (STEPS.length - 1)) * 100}%`,
              background: 'linear-gradient(90deg, #10B981 0%, #E23744 100%)',
              borderRadius: 'var(--radius-full)',
              transition: 'width 0.5s cubic-bezier(0.4, 0, 0.2, 1)'
            }}
          />
        </div>

        {/* Stepper Grid Columns */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            position: 'relative',
            gap: '0.75rem',
            zIndex: 2
          }}
          className="stepper-grid"
        >
          {STEPS.map((step, idx) => {
            const isCompleted = idx < activeIndex;
            const isCurrent = idx === activeIndex;

            return (
              <div
                key={step.key}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  textAlign: 'center'
                }}
              >
                {/* Step Circle with Halo Glow */}
                <div
                  style={{
                    width: '48px',
                    height: '48px',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '1.25rem',
                    fontWeight: 700,
                    transition: 'all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)',
                    backgroundColor: isCompleted
                      ? 'var(--accent-basil)'
                      : isCurrent
                      ? 'var(--accent-tomato)'
                      : '#FFFFFF',
                    border: isCompleted || isCurrent ? 'none' : '2px solid #CBD5E1',
                    color: isCompleted || isCurrent ? '#FFFFFF' : '#94A3B8',
                    boxShadow: isCurrent
                      ? '0 0 0 6px rgba(226, 55, 68, 0.22), var(--shadow-md)'
                      : isCompleted
                      ? '0 0 0 4px rgba(16, 185, 129, 0.15)'
                      : 'var(--shadow-sm)'
                  }}
                  className={isCurrent ? 'pulse' : ''}
                >
                  {isCompleted ? '✓' : step.icon}
                </div>

                {/* Step Title */}
                <div
                  style={{
                    marginTop: '0.65rem',
                    fontSize: '0.88rem',
                    fontWeight: isCurrent ? 800 : isCompleted ? 700 : 600,
                    color: isCurrent
                      ? 'var(--accent-tomato)'
                      : isCompleted
                      ? 'var(--accent-basil)'
                      : 'var(--text-muted)'
                  }}
                >
                  {step.label}
                </div>

                {/* Step Mini Description */}
                <div
                  style={{
                    fontSize: '0.74rem',
                    color: isCurrent ? 'var(--text-main)' : 'var(--text-light)',
                    marginTop: '0.2rem',
                    fontWeight: isCurrent ? 500 : 400
                  }}
                  className="hide-on-mobile"
                >
                  {step.desc}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <style>{`
        @media (max-width: 640px) {
          .stepper-grid {
            grid-template-columns: repeat(2, 1fr) !important;
            row-gap: 1.5rem;
          }
          .hide-on-mobile {
            display: none !important;
          }
        }
      `}</style>
    </div>
  );
};

export default OrderStepper;

import React from 'react';
import { RoleName } from '../types';

export const RoleBadge: React.FC<{ role: RoleName | string }> = ({ role }) => {
  const styles: Record<string, string> = {
    ADMIN: 'bg-purple-100 text-purple-800 border-purple-200',
    MANAGER: 'bg-blue-100 text-blue-800 border-blue-200',
    EMPLOYEE: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    CUSTOMER: 'bg-amber-100 text-amber-800 border-amber-200',
  };

  const style = styles[role] || 'bg-gray-100 text-gray-800 border-gray-200';

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold border ${style}`}>
      {role}
    </span>
  );
};
